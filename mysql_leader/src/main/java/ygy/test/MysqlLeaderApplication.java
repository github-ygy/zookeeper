package ygy.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("ygy.test.DAL.mapper")
public class MysqlLeaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MysqlLeaderApplication.class, args);
	}
}
