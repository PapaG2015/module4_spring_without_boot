package ru.practicum.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;

import org.springframework.web.context.WebApplicationContext;
import ru.practicum.config.PersistenceConfig;
import ru.practicum.config.WebConfig;
import ru.practicum.item.dao.ItemRepository;
import ru.practicum.item.dao.ItemRepositoryImpl;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.TestPropertySource;
import ru.practicum.user.*;

@Transactional
@TestPropertySource(properties = {"db.name=test"})
@SpringJUnitWebConfig({ItemController.class, WebConfig.class, ItemServiceImpl.class, PersistenceConfig.class,
        UrlMetaDataRetrieverImpl.class})
class ItemControllerTestWithContextPOSTMAN {
    private final ObjectMapper mapper = new ObjectMapper();

    private final ItemService itemService;

    private MockMvc mvc;

    private final UserRepository userRepository;

    private AddItemRequest addItemRequest;

    private ItemDto itemDto;

    private User user;

    @Autowired
    ItemControllerTestWithContextPOSTMAN(ItemService itemService, UserRepository userRepository) {
        this.itemService = itemService;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        addItemRequest = new AddItemRequest();
        addItemRequest.setUrl("http://www.slabotochka66.ru/");
        addItemRequest.getTags().add("marat");

        itemDto = ItemDto.builder()
                .id(1L)
                .normalUrl("yandex.ru")
                .resolvedUrl("yandex.ru")
                .mimeType("mimeType")
                .title("title")
                .hasImage(true)
                .hasVideo(true)
                .unread(true)
                .dateResolved("dateResolved")
                .tags(null)
                .build();

        user = new User();
        user.setFirstName("marat");
    }

    @Test
    void testAddAndGet() throws Exception {

        User userDB = userRepository.save(user);

        //ItemDto itemDtoWeb;

        mvc.perform(post("/items")
                .header("X-Later-User-Id", userDB.getId())
                .content(mapper.writeValueAsString(addItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalUrl", is("http://www.slabotochka66.ru/")));


        //Получение
        class Id implements ResultHandler {
            Long id;

            @Override
            public void handle(MvcResult mvcResult) {
                try {
                    String[] s1 = mvcResult.getResponse().getContentAsString().split(":");
                    String[] s2 = s1[1].split(",");
                    id = Long.parseLong(s2[0]);
                } catch (UnsupportedEncodingException e) {

                }
            }

        }

        Id id = new Id();


        mvc.perform(get("/items")
                .header("X-Later-User-Id", userDB.getId())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(id)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].normalUrl", is("http://www.slabotochka66.ru/")));

        //Удаление
        mvc.perform(delete("/items/{id}", id.id)
                .header("X-Later-User-Id", userDB.getId()))
                .andExpect(status().isOk());

        mvc.perform(get("/items")
                .header("X-Later-User-Id", userDB.getId())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        //Запись новой ссылки
        mvc.perform(post("/items")
                .header("X-Later-User-Id", userDB.getId())
                .content(mapper.writeValueAsString(addItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalUrl", is("http://www.slabotochka66.ru/")))
                .andExpect(jsonPath("$.unread", is(true)));

        mvc.perform(get("/items")
                .header("X-Later-User-Id", userDB.getId())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(id)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].normalUrl", is("http://www.slabotochka66.ru/")))
                .andExpect(jsonPath("$[0].tags[0]", is("marat")));

        //изменение
        ModifyItemRequest modifyItemRequest = new ModifyItemRequest();
        modifyItemRequest.setItemId(id.id);
        Set<String> tags = new HashSet<>();
        tags.add("ГОРОД");
        modifyItemRequest.setReplaceTags(true);
        modifyItemRequest.setTags(tags);

        mvc.perform(patch("/items")
                .header("X-Later-User-Id", userDB.getId())
                .content(mapper.writeValueAsString(modifyItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //запрос
        mvc.perform(get("/items")
                .header("X-Later-User-Id", userDB.getId())
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].normalUrl", is("http://www.slabotochka66.ru/")))
                .andExpect(jsonPath("$[0].tags[0]", is("ГОРОД")));

        //Изменение без прав
        mvc.perform(patch("/items")
                .header("X-Later-User-Id", 400)
                .content(mapper.writeValueAsString(modifyItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}