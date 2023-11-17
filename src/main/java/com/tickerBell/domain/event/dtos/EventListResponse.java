package com.tickerBell.domain.event.dtos;

import com.querydsl.core.annotations.QueryProjection;
import com.tickerBell.domain.casting.entity.Casting;
import com.tickerBell.domain.event.entity.Category;
import com.tickerBell.domain.event.entity.Event;
import com.tickerBell.domain.image.entity.Image;
import com.tickerBell.domain.utils.SeatPriceCalculator;
import com.tickerBell.global.exception.CustomException;
import com.tickerBell.global.exception.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventListResponse {
    private Long eventId;
    private String name; // 이벤트 이름
    private LocalDateTime startEvent; // 이벤트 시작 시간
    private Float saleDegree; // 세일
    private Integer normalPrice; // 일반 좌석 가격
    private Float discountNormalPrice; // 세일 후 특수석 좌석 가격
    private Category category;
    private String thumbNailUrl; // 썸네일 (service 에서 추가)
    private List<String> castings; // 캐스팅 정보 (service 에서 추가)

    //todo: 리스트에 반환할 데이터 더 필요하다면 추가

    public static EventListResponse from(Event event) {
        List<String> castings = new ArrayList<>();
        for (Casting findCasting : event.getCastingList()) {
            castings.add(findCasting.getCastingName());
        }

        List<Image> thumbnailImage = event.getImageList().stream()
                .filter(Image::getIsThumbnail)
                .collect(Collectors.toList());

        return EventListResponse.builder()
                .eventId(event.getId())
                .name(event.getName())
                .startEvent(event.getStartEvent())
                .saleDegree(event.getSaleDegree())
                .normalPrice(event.getNormalPrice())
                .discountNormalPrice(SeatPriceCalculator.getSeatPrice(event.getSaleDegree(), event.getNormalPrice()))
                .category(event.getCategory())
                .thumbNailUrl((thumbnailImage != null && !thumbnailImage.isEmpty()) ? thumbnailImage.get(0).getS3Url() : null)
                .castings(castings)
                .build();
    }

    @QueryProjection
    public EventListResponse(Long eventId, String name, LocalDateTime startEvent, String thumbNailUrl, Integer normalPrice, Float saleDegree, Category category) {
        this.eventId = eventId;
        this.name = name;
        this.startEvent = startEvent;
        this.thumbNailUrl = thumbNailUrl;
        this.normalPrice = normalPrice;
        this.saleDegree = saleDegree;
        this.category = category;
    }

    @QueryProjection
    public EventListResponse(Event e) {
        List<String> castings = new ArrayList<>();
        for (Casting findCasting : e.getCastingList()) {
            castings.add(findCasting.getCastingName());
        }

        List<Image> thumbnailImage = e.getImageList().stream()
                .filter(Image::getIsThumbnail)
                .collect(Collectors.toList());
        this.eventId = e.getId();
        this.name = e.getName();
        this.startEvent = e.getStartEvent();
        this.saleDegree = e.getSaleDegree();
        this.normalPrice = e.getNormalPrice();
        this.discountNormalPrice = SeatPriceCalculator.getSeatPrice(e.getSaleDegree(), e.getNormalPrice());
        this.category = e.getCategory();
        this.thumbNailUrl = (thumbnailImage != null && !thumbnailImage.isEmpty()) ? thumbnailImage.get(0).getS3Url() : null;
        this.castings = castings;
    }
}
