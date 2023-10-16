package com.tickerBell.domain.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tickerBell.domain.image.entity.Image;
import com.tickerBell.domain.image.repository.ImageRepository;
import com.tickerBell.global.exception.CustomException;
import com.tickerBell.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    // 단위 테스트할 때 사용
    // @Mock 으로 만들어진 인스턴스들을 자동으로 주입
    @InjectMocks
    private ImageServiceImpl imageService;

    // MockBean 스프링 컨텍스트에 등록함 -> 통합테스트에서 사용
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private AmazonS3Client amazonS3Client;

    @DisplayName("이미지 업로드 및 조회")
    @Test
    public void testUploadImage() throws IOException {
        // given
        MultipartFile thumbnailImage = new MockMultipartFile("thumbnail.jpg", "thumbnail.jpg", "image/jpeg", new byte[0]);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(new MockMultipartFile("image1.jpg", "image1.jpg", "image/jpeg", new byte[0]));
        imageFiles.add(new MockMultipartFile("image2.png", "image2.png", "image/png", new byte[0]));

        // stub
        when(amazonS3Client.getUrl(any(), any())).thenReturn(new URL("https://example-image.jpg"));
        when(amazonS3Client.putObject(any())).thenReturn(any());

        // when
        List<Image> imageList = imageService.uploadImage(thumbnailImage, imageFiles);

        // then
        verify(amazonS3Client, times(3)).putObject(any(PutObjectRequest.class)); // s3 업로드 횟수 확인
        verify(amazonS3Client, times(3)).getUrl(any(), any()); // s3 조회 횟수 확인
    }

    @DisplayName("이미지 삭제")
    @Test
    public void testDeleteImage() throws IOException {
        // given
        List<Image> imageList = new ArrayList<>();
        Image image1 = Image.builder()
                .originImgName("image1.jpg")
                .build();
        Image image2 = Image.builder()
                .originImgName("image2.jpg")
                .build();
        imageList.add(image1);
        imageList.add(image2);

        // stub
        doNothing().when(amazonS3Client).deleteObject(any(DeleteObjectRequest.class));
        doNothing().when(imageRepository).deleteAll(any(List.class));

        // when
        imageService.deleteImage(imageList);

        // then
        verify(amazonS3Client, times(2)).deleteObject(any(DeleteObjectRequest.class)); // s3 삭제  횟수 확인
        verify(imageRepository, times(1)).deleteAll(any(List.class));
    }

    @DisplayName("사진이 아닌 확장자 예외 처리")
    @Test
    public void testInvalidFileExtension() throws IOException {
        // given
        MultipartFile thumbnailImage = new MockMultipartFile("thumbnail.jpg", "thumbnail.jpg", "application/octet-stream", new byte[0]);
        List<MultipartFile> emptyMultipartFiles = new ArrayList<>();

        // when
        assertThatThrownBy(() -> imageService.uploadImage(thumbnailImage, emptyMultipartFiles))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_SUPPORTED_EXTENSION);

        // then
        verifyNoInteractions(amazonS3Client);
    }

    @DisplayName("확장자가 없는 파일 예외 처리")
    @Test
    public void testNullExtension() throws IOException {
        // given
        MultipartFile thumbnailImage = new MockMultipartFile("thumbnail.jpg", "thumbnail.jpg", "", new byte[0]);
        List<MultipartFile> emptyMultipartFiles = new ArrayList<>();

        // when
        assertThatThrownBy(() -> imageService.uploadImage(thumbnailImage, emptyMultipartFiles))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_FOUND_EXTENSION);

        // then
        verifyNoInteractions(amazonS3Client);
    }

    @Test
    @DisplayName("이벤트 PK로 조회 테스트")
    void findByEventIdTest() {
        // given
        Long eventId = 1L;
        List<Image> imageList = new ArrayList<>();
        imageList.add(Image.builder().build());

        // stub
        when(imageRepository.findImageByEventId(eventId)).thenReturn(imageList);

        // when
        List<Image> findImages = imageService.findByEventId(eventId);

        // then
        assertThat(findImages).isNotNull();
        assertThat(findImages.size()).isEqualTo(1);
    }
}