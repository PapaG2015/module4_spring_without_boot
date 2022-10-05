package ru.practicum.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.config.WebConfig;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringJUnitConfig({UrlMetaDataRetrieverImpl.class})
public class UrlMetaDataRetrieverTest {

    private final UrlMetaDataRetriever urlMetaDataRetriever;

    @Test
    void getGoodURL() {
        UrlMetaDataRetriever.UrlMetadata result = urlMetaDataRetriever
                .retrieve("http://www.slabotochka66.ru/");

        Assertions.assertEquals("http://www.slabotochka66.ru/",
                result.getNormalUrl());

        Assertions.assertEquals("text", result.getMimeType());

        Assertions.assertEquals("МТЦ Кристалл: системы безопасности, NFC визитки, прокат инструмента"
                , result.getTitle());

        Assertions.assertEquals(true, result.isHasImage());

        Assertions.assertEquals(false, result.isHasVideo());
    }

    @Test
    void getBadURL() {

        final ItemRetrieverException exception = Assertions.assertThrows(
                ItemRetrieverException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UrlMetaDataRetriever.UrlMetadata result = urlMetaDataRetriever.retrieve("\"https://practicum.yandex.ru/java-developer/\"");
                    }
                }
        );
        Assertions.assertEquals("The URL is malformed: " + "\"https://practicum.yandex.ru/java-developer/\"", exception.getMessage());
    }

    @Test
    void getNoURL() {

        final ItemRetrieverException exception = Assertions.assertThrows(
                ItemRetrieverException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UrlMetaDataRetriever.UrlMetadata result = urlMetaDataRetriever.retrieve("http://www.slabotochka6a.ru/");
                    }
                }
        );
        Assertions.assertEquals("Cannot retrieve data from the URL: " + "http://www.slabotochka6a.ru/", exception.getMessage());
    }

    @Test
    void getUnauthorizedURL() {

        final ItemRetrieverException exception = Assertions.assertThrows(
                ItemRetrieverException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UrlMetaDataRetriever.UrlMetadata result = urlMetaDataRetriever.retrieve("https://httpstat.us/401");
                    }
                }
        );
        Assertions.assertEquals("There is no access to the resource at the specified URL: " + "https://httpstat.us/401", exception.getMessage());
    }
}
