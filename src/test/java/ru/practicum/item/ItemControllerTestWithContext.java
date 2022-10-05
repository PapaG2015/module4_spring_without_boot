package ru.practicum.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.config.WebConfig;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig({ItemController.class, ItemControllerTestConfig.class, WebConfig.class})
class ItemControllerTestWithContext {
    private final ObjectMapper mapper = new ObjectMapper();

    private final ItemService itemService;

    private MockMvc mvc;

    private AddItemRequest addItemRequest;

    private ItemDto itemDto;

    @Autowired
    ItemControllerTestWithContext(ItemService itemService) {
        this.itemService = itemService;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        addItemRequest = new AddItemRequest();
        addItemRequest.setUrl("yandex.ru");

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
    }

    @Test
    void add() throws Exception {
        when(itemService.addNewItem(Mockito.anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                    .header("X-Later-User-Id", 1L)
                    .content(mapper.writeValueAsString(addItemRequest))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.normalUrl", is(itemDto.getNormalUrl())))
                .andExpect(jsonPath("$.mimeType", is(itemDto.getMimeType())))
                .andExpect(jsonPath("$.dateResolved", is(itemDto.getDateResolved())));
    }

    @Test
    void addWithoutHeaderX() throws Exception {
        when(itemService.addNewItem(Mockito.anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                .content(mapper.writeValueAsString(addItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void getItems() throws Exception {
        List<ItemDto> itemDtos = new ArrayList<>();

        when(itemService.getItems(any()))
                .thenReturn(itemDtos);

        mvc.perform(get("/items")
                    .header("X-Later-User-Id", 1L)
                    .content(mapper.writeValueAsString(addItemRequest))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteItem() throws Exception {

        doNothing().when(itemService).deleteItem(Mockito.anyLong(), Mockito.anyLong());

        mvc.perform(delete("/items/3")
                    .header("X-Later-User-Id", 1L)
                    .content(mapper.writeValueAsString(addItemRequest))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void modifyItem() throws Exception {

        ModifyItemRequest modifyItemRequest = new ModifyItemRequest();

        when(itemService.changeItem(Mockito.anyLong(), Mockito.any(ModifyItemRequest.class)))
                .thenReturn(itemDto);

        mvc.perform(patch("/items")
                .header("X-Later-User-Id", 1L)
                .content(mapper.writeValueAsString(modifyItemRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}