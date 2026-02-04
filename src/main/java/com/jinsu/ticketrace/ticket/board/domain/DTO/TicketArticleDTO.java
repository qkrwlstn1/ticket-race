package com.jinsu.ticketrace.ticket.board.domain.DTO;


import com.jinsu.ticketrace.ticket.board.domain.entity.TicketBoard;
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

        private LocalDateTime deadlineDate;
        private LocalDateTime createDate;

        private String memberNickname;

        public static CreateArticleResponse of(TicketBoard ticketBoard){
            return CreateArticleResponse.builder()
                    .boardPk(ticketBoard.getBoardPk())
                    .title(ticketBoard.getTitle())
                    .content(ticketBoard.getContent())
                    .deadlineDate(ticketBoard.getDeadlineDateTime())
                    .createDate(ticketBoard.getCreateDateTime())
                    .memberNickname(ticketBoard.getMember().getNickname())
                    .build();
        }

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetArticle{
        private String title;
        private String content;

        private LocalDateTime deadlineDate;
        private LocalDateTime modifyDate;
        private LocalDateTime createDate;

        private String memberNickname;
        public static GetArticle of(TicketBoard ticketBoard){
            return GetArticle.builder()
                    .title(ticketBoard.getTitle())
                    .content(ticketBoard.getContent())
                    .deadlineDate(ticketBoard.getDeadlineDateTime())
                    .modifyDate(ticketBoard.getModifyDateTime())
                    .createDate(ticketBoard.getCreateDateTime())
                    .memberNickname(ticketBoard.getMember().getNickname())
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
        @NotBlank
        private long boardPk;
        @NotBlank
        @Size(max = 255)
        private String title;
        @NotBlank
        @Size(max = 5000)
        private String content;
        @NotNull
        @Future
        private LocalDateTime deadlineDateTime;
    }

}
