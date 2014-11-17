package ro.fortsoft.pippo.demo.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.demo.crud.InMemoryContactService;

/**
 * @author Decebal Suiu
 */
@Configuration
public class SpringConfiguration {

    @Bean
    public ContactService contactService(){
        return new InMemoryContactService();
    }

}
