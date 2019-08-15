package com.zp.v1;

import com.zp.annoction.MyAutowired;
import com.zp.annoction.MyController;
import com.zp.annoction.MyRequestMapping;
import com.zp.annoction.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @创建人 zp
 * @创建时间 2019/8/14
 * @描述 手写一个简单的Spring
 */
public class ZpDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> ioc = new HashMap<>();

    private Map<String,Object> handleMapping = new HashMap<>();

    private Map<String,Object> controllerMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            //处理请求
            doDispatch(req,resp);
        }catch (Exception e){
            resp.getWriter().write("500 Service Exception !" + e.getMessage());
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if(handleMapping.isEmpty()){return;}

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replace(contextPath,"").replaceAll("/+","/");
        if(!this.handleMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND !");
            return;
        }

        Method method = (Method)this.handleMapping.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求参数
        Map<String,String[]> parameterMap = req.getParameterMap();
        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        //方法的参数列表
        for (int i = 0; i<parameterTypes.length; i++){
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();

            if (requestParam.equals("HttpServletResponse")){
                paramValues[i]=resp;
                continue;
            }

            if (requestParam.equals("HttpServletRequest")){
                //参数类型已明确，这边强转类型
                paramValues[i]=req;
                continue;
            }
            if(requestParam.equals("String")){
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i]=value;
                }
            }
        }
        try{
            String simpleName = toLowerFirstWord(method.getDeclaringClass().getSimpleName());
            method.invoke(this.ioc.get(simpleName),paramValues);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(properties.getProperty("scanPackage"));

        //3、拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();

        //4、将初始化到IOC容器中的类，需要赋值的字段进行赋值
        doAutowired();

        //5、初始化HandleMapping
        initHandleMapping();

    }

    /**
     * 将初始化到IOC容器中的类，需要赋值的字段进行赋值
     */
    private void doAutowired() {

        if(ioc.isEmpty()){return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()){
            //拿到对象中的所有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields){

                if(!field.isAnnotationPresent(MyAutowired.class)){continue;}

                MyAutowired annotation = field.getAnnotation(MyAutowired.class);
                String simpleName = annotation.value().trim();
                if("".equals(simpleName)){
                    simpleName = field.getType().getName();
                }
                //设置私有属性的访问权限
                field.setAccessible(true);

                try{
                    field.set(entry.getValue(),ioc.get(simpleName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 初始化HandleMapping
     */
    private void initHandleMapping() {
        if(ioc.isEmpty()){return;}
        try{
            for ( Map.Entry<String, Object> entry : ioc.entrySet()){
                Class<?> clazz = entry.getValue().getClass();
                if(!clazz.isAnnotationPresent(MyController.class)){continue;}
                //拼url时,是controller头的url拼上方法上的url
                String baseUrl = "";
                if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = annotation.value();
                }
                //获取类中所有的方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods){
                    if(!method.isAnnotationPresent(MyRequestMapping.class)){continue;}
                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();

                    url = (baseUrl + "/" + url).replaceAll("/+","/");
                    handleMapping.put(url,method);
                    //controllerMap.put(url,clazz.newInstance());
                    System.out.println(url + "," + method);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
     */
    private void doInstance() {
        //如果没有扫描到类，直接返回
        if(classNames.isEmpty()){
            return;
        }

        for (String className : classNames){
            try {
                //把类反射出来实例化
                Class<?> clazz = Class.forName(className);
                String simpleName = toLowerFirstWord(clazz.getSimpleName());
                if(clazz.isAnnotationPresent(MyController.class)){
                    MyController annotation = clazz.getAnnotation(MyController.class);
                    if(!"".equals(annotation.value())){
                        simpleName = annotation.value();
                    }
                    ioc.put(simpleName,clazz.newInstance());
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    MyService annotation = clazz.getAnnotation(MyService.class);
                    if(!"".equals(annotation.value())){
                        simpleName = annotation.value();
                        ioc.put(simpleName,clazz.newInstance());
                        continue;
                    }
                    ioc.put(simpleName,clazz.newInstance());

                    //
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class c : interfaces){
                        ioc.put(c.getName(),clazz.newInstance());
                    }
                }else{
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

    }

    private String toLowerFirstWord(String className) {
        char[] chars = className.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    /**
     * 初始化IOC容器，扫描相关的类
     * @param packageName
     */
    private void doScanner(String packageName) {

        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()){
            if(file.isDirectory()){
                //递归读取包
                doScanner(packageName + "." + file.getName());
            }else {
                if(!file.getName().endsWith(".class")){ continue; }
                String className =packageName +"." +file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     * @param location
     */
    private void doLoadConfig(String location) {
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try{
            //使用properties加载文件里面内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != resourceAsStream){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
