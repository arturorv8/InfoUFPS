package org.infoufps.application.content;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infoufps.domain.content.CommentTargetType;
import org.infoufps.domain.content.ICommentRepository;
import org.infoufps.domain.content.IContentRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GetModuleQuery {

    private final IContentRepository repository;
    private final ICommentRepository commentRepository;

    @Inject
    public GetModuleQuery(IContentRepository repository, ICommentRepository commentRepository) {
        this.repository = repository;
        this.commentRepository = commentRepository;
    }

    public GetModuleQuery(IContentRepository repository) {
        this(repository, null);
    }

    public Map<String, List<ModuleItemDto>> execute() {
        return repository.findAllPublished().stream()
                .map(content -> {
                    int count = commentRepository != null
                            ? (int) commentRepository.countPublishedByTarget(CommentTargetType.CONTENT, content.getId().value())
                            : content.getStats().getCommentsCount();
                    return ModuleItemDto.from(content, count);
                })
                .collect(Collectors.groupingBy(
                        dto -> dto.categoryTag() != null ? dto.categoryTag() : "GENERAL",
                        LinkedHashMap::new,
                        Collectors.toList()));
    }
}
