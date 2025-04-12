package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.payload.feedback.FeedbackResponse;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FeedbackMapper {

    @Mapping(target = "owner.id", source = "userInfo.id")
    @Mapping(target = "owner.firstName", source = "userInfo.firstName")
    @Mapping(target = "owner.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "feedback.id")
    @BeanMapping(qualifiedByName = "addLinks")
    FeedbackResponse toResponse(Feedback feedback, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default FeedbackResponse addLinks(Feedback feedback, @MappingTarget FeedbackResponse response) {

        response.add(Link.of("/feedbacks/" + feedback.getId()).withSelfRel());
        response.add(Link.of("/feedbacks/"  + feedback.getId()).withRel("update"));
        response.add(Link.of("/feedbacks/"  + feedback.getId()).withRel("delete"));

        return response;
    }
}
