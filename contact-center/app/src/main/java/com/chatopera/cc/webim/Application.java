/*
 * Copyright (C) 2017 优客服-多渠道客服系统
 * Modifications copyright (C) 2018 Chatopera Inc, <https://www.chatopera.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chatopera.cc.webim;

import com.chatopera.cc.core.UKDataContext;
import com.chatopera.cc.util.mobile.MobileNumberUtils;
import com.chatopera.cc.webim.config.web.StartedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;

@SpringBootApplication
@EnableJpaRepositories("com.chatopera.cc.webim.service.repository")
@EnableElasticsearchRepositories("com.chatopera.cc.webim.service.es")
@EnableAsync
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);


    @Value("${web.upload-path}")
    private String uploaddir;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String multipartMaxUpload;


    @Value("${spring.servlet.multipart.max-request-size}")
    private String multipartMaxRequest;

	static{
    	UKDataContext.model.put("contacts", true) ;
    	UKDataContext.model.put("sales", true);
    	UKDataContext.model.put("chatbot", true);
    }

    /**
     * Init local resources
     */
    protected static void init(){
        try {
            logger.info("init mobile number utils ...");
            MobileNumberUtils.init();
        } catch (IOException e) {
            logger.error("init error ", e);
            System.exit(1);
        }
    }

    @Bean   
    public MultipartConfigElement multipartConfigElement() {   
            MultipartConfigFactory factory = new MultipartConfigFactory();  
            factory.setMaxFileSize(multipartMaxUpload); //KB,MB
            factory.setMaxRequestSize(multipartMaxRequest);
            factory.setLocation(uploaddir);
            return factory.createMultipartConfig();   
    }   
      
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {

        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
            	ErrorPage error = new ErrorPage("/error.html");
            	container.addErrorPages(error);
            }
        };
    }
    
	public static void main(String[] args) {
        Application.init();
		SpringApplication app = new SpringApplication(Application.class) ;
		app.setBannerMode(Banner.Mode.OFF);
        app.setAddCommandLineProperties(false);
		app.addListeners(new StartedEventListener());
		UKDataContext.setApplicationContext(app.run(args));
	}	
}

