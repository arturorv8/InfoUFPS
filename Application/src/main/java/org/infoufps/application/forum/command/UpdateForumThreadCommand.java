package org.infoufps.application.forum.command;

public record UpdateForumThreadCommand(
        String threadId,
        String actorAccountId,
        boolean isAdmin,
        String title,
        String body
) {
}

