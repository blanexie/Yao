package xyz.xiezc.ioc.starter.orm.example;

import xyz.xiezc.ioc.starter.Yao;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;

import java.util.Arrays;
import java.util.List;

public class JpaApp {


    public static void main(String[] args) {
        ApplicationContext applicationContext = Yao.run(JpaApp.class);
        BeanDefinition beanDefinition = applicationContext.getBeanDefinition(UserRepository.class);
        UserRepository userRepository = beanDefinition.getBean();
        UserTest userTest= new UserTest();
        userTest.setName("xzc");
        userRepository.save(Arrays.asList(userTest));
        userTest.setId(null);
        userRepository.save(Arrays.asList(userTest));
        userTest.setId(null);
        userRepository.save(Arrays.asList(userTest));
        userTest.setId(null);
        userRepository.save(Arrays.asList(userTest));

        List<UserTest> userTests = userRepository.find();
        for (UserTest test : userTests) {
            System.out.println(test);
        }

    }
}
