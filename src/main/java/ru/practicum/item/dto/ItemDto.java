package ru.practicum.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
public class ItemDto {
    private final Long id;
    private final String normalUrl;
    private final String resolvedUrl;
    private final String mimeType;
    private final String title;
    private final boolean hasImage;
    private final boolean hasVideo;
    private final boolean unread;
    private final String dateResolved;
    private final Set<String> tags;


    public ItemDto(Long id, String normalUrl, String resolvedUrl, String mimeType, String title, boolean hasImage, boolean hasVideo, boolean unread, String dateResolved, Set<String> tags) {
        this.id = id;
        this.normalUrl = normalUrl;
        this.resolvedUrl = resolvedUrl;
        this.mimeType = mimeType;
        this.title = title;
        this.hasImage = hasImage;
        this.hasVideo = hasVideo;
        this.unread = unread;
        this.dateResolved = dateResolved;
        this.tags = tags;
    }
}