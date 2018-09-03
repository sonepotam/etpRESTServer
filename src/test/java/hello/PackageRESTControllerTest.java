package hello;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import ru.abr.etp.controller.PackageRESTController;
import ru.abr.etp.service.DataPackageConsumer;
import ru.abr.etp.service.DataPackageConsumerImpl;
import ru.abr.utils.ClientListHolder;
import ru.abr.utils.Config;
import ru.abr.utils.utils;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = {PackageRESTController.class, DataPackageConsumerImpl.class})
public class PackageRESTControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PackageRESTController controller;

    private static final CharacterEncodingFilter CHARACTER_ENCODING_FILTER = new CharacterEncodingFilter();
    private static final String REST_URL = "/callback";

    static {
        CHARACTER_ENCODING_FILTER.setEncoding("UTF-8");
        CHARACTER_ENCODING_FILTER.setForceEncoding(true);
    }


    @PostConstruct
    void postConstruct() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(CHARACTER_ENCODING_FILTER)
                .build();
    }

    @Test
    public void testClientListProcessing() throws Exception {



        String fileName = "C:\\Java\\etp\\files\\from_etp\\fundsholdrq.xml";
        String content = utils.readFile( fileName, "utf-8");

        DataPackageConsumer consumer = new DataPackageConsumerImpl();
        StringBuffer buffer = new StringBuffer();
        Config config = Config.getInstance();
        ClientListHolder holder = ClientListHolder.getInstance();

        holder.readClients( "C:\\Java\\etp\\files\\to_etp\\bank_CLIENT_LIST.xml" );

        consumer.process( content, buffer);


    }






}
