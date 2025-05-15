package com.petshop;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
@MapperScan("com.petshop.mapper") // 指定 Mapper 接口所在的包
class PetshopApplicationTests {

	@Test
	void contextLoads() {
	}

}
