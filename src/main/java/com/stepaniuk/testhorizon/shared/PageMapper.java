package com.stepaniuk.testhorizon.shared;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PageMapper {

    default <T> PagedModel<T> toResponse(Page<T> page, URI base) {
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(page.getSize(), page.getNumber(),
                page.getTotalElements(), page.getTotalPages());
        PagedModel<T> model = PagedModel.of(page.getContent(), metadata);
        boolean isNavigable = page.hasPrevious() || page.hasNext();

        if (isNavigable) {
            model.add(createLink(base, PageRequest.of(0, page.getSize(), page.getSort()),
                    IanaLinkRelations.FIRST));
        }

        if (page.hasPrevious()) {
            model.add(createLink(base, page.previousPageable(), IanaLinkRelations.PREV));
        }

        Link selfLink = createLink(base, page.getPageable(), IanaLinkRelations.SELF);

        model.add(selfLink);

        if (page.hasNext()) {
            model.add(createLink(base, page.nextPageable(), IanaLinkRelations.NEXT));
        }

        if (isNavigable) {

            int lastIndex = page.getTotalPages() == 0 ? 0 : page.getTotalPages() - 1;

            model.add(createLink(base, PageRequest.of(lastIndex, page.getSize(), page.getSort()),
                    IanaLinkRelations.LAST));
        }

        return model;
    }

    default Link createLink(URI base, Pageable pageable, LinkRelation relation) {
        UriComponentsBuilder builder = fromUri(base);
        builder.replaceQueryParam("page", pageable.getPageNumber());
        builder.replaceQueryParam("size", pageable.getPageSize());
        List<String> sort = pageable.getSort().stream().map(order -> order.getProperty() + "," + order.getDirection())
                .toList();
        builder.replaceQueryParam("sort", sort.toArray());
        return Link.of(UriTemplate.of(builder.build().toString()), relation);
    }

}