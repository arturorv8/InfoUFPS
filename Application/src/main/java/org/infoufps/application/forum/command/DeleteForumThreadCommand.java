package org.infoufps.application.forum.command;

public record DeleteForumThreadCommand(
        String threadId,
        String actorAccountId,
        boolean isAdmin
) {
}

