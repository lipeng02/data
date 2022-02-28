package com.sansi.data;

import com.sansi.data.netty.BootNettyServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan("com.sansi.data.dao")
@SpringBootApplication
public class DataApplication implements CommandLineRunner {
    @Autowired
    private BootNettyServer nettyServer;

    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
// 使用异步注解方式启动netty服务端服务
        this.nettyServer.bind();
    }
}
