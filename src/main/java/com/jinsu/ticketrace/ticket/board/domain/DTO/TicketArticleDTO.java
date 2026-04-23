package com.jinsu.ticketrace.ticket.board.domain.DTO;


import com.jinsu.ticketrace.ticket.board.domain.entity.GATicketBoard;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class TicketArticleDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateArticleRequest{

        @NotBlank
        @Size(max = 255)
        private String title;
        @NotBlank
        @Size(max = 5000)
        private String content;
        @NotNull
        @Min(0)
        private long price;
        @NotNull
        @Min(0)
        private long quantity;
        @NotNull
        @Future
        private LocalDateTime deadline;
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateArticleResponse{
        private long boardPk;
        private String title;
        private String content;
        private long quantity;
        private long price;
        private LocalDateTime deadlineDate;
        private LocalDateTime createDate;

        private String memberNickname;

        public static CreateArticleResponse of(GATicketBoard GATicketBoard){
            return CreateArticleResponse.builder()
                    .boardPk(GATicketBoard.getBoardPk())
                    .title(GATicketBoard.getTitle())
                    .content(GATicketBoard.getContent())
                    .quantity(GATicketBoard.getQuantity())
                    .price(GATicketBoard.getPrice())
                    .deadlineDate(GATicketBoard.getDeadlineDateTime())
                    .createDate(GATicketBoard.getCreateDateTime())
                    .memberNickname(GATicketBoard.getMember().getNickname())
                    .build();
        }

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetArticle{
        private long boardPk;
        private String title;
        private String content;
        private long quantity;
        private long price;

        private LocalDateTime deadlineDate;
        private LocalDateTime modifyDate;
        private LocalDateTime createDate;

        private String memberNickname;
        public static GetArticle of(GATicketBoard GATicketBoard){
            return GetArticle.builder()
                    .boardPk(GATicketBoard.getBoardPk())
                    .quantity(GATicketBoard.getQuantity())
                    .title(GATicketBoard.getTitle())
                    .content(GATicketBoard.getContent())
                    .deadlineDate(GATicketBoard.getDeadlineDateTime())
                    .modifyDate(GATicketBoard.getModifyDateTime())
                    .createDate(GATicketBoard.getCreateDateTime())
                    .memberNickname(GATicketBoard.getMember().getNickname())
                    .build();
        }
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetSummaryArticle{
        private String title;

        private LocalDateTime deadlineDate;
        private LocalDateTime createDate;

        private String memberNickname;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModifyArticleRequest{
        @Min(0)
        private long boardPk;
        @NotBlank
        @Size(max = 255)
        private String title;
        @NotBlank
        @Size(max = 5000)
        private String content;
        @Min(0)
        @NotNull
        private long price;
        @Min(0)
        @NotNull
        private long quantity;
        @NotNull
        @Future
        private LocalDateTime deadlineDateTime;
    }

}
