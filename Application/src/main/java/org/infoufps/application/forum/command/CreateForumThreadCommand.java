package org.infoufps.application.forum.command;

public record CreateForumThreadCommand(
        String title,
        String body,
        String authorId
) {
}

