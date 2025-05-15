package com.petshop.config;
import com.petshop.common.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;



    // 从配置文件中注入上传目录路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    public WebConfig() {
        System.out.println("WebConfig 已加载！"); // 确保控制台打印此消息
    }

    //解决 CORS 跨域问题（后端配置）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Upgrade", "Connection") // 允许协议升级头
                .exposedHeaders("Upgrade", "Connection")  // 允许客户端访问这些头
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")          // 拦截所有API路径
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/uploads/pet_images/**", // 关键：放行图片路径
                        "/error",
                        "/ws-chat"  // ✅ 放行 WebSocket 握手路径
                );
    }
    // 配置静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理文件上传路径映射
        registry.addResourceHandler("/uploads/pet_images/**")
                .addResourceLocations("file:" + uploadDir + "/") // 注意路径末尾的斜杠
                .setCachePeriod(3600); // 建议生产环境启用缓存

        // 添加默认静态资源处理（可选）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    // Thymeleaf视图解析器
    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1); // 设置解析器优先级
        return resolver;
    }

    // 模板解析器配置
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/"); // 确保模板存放目录正确
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // 开发阶段禁用缓存
        return resolver;
    }

    // 模板引擎配置
    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        engine.setEnableSpringELCompiler(true); // 启用Spring EL表达式编译
        return engine;
    }

}