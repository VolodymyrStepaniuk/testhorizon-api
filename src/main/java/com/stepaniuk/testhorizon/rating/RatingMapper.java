package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RatingMapper {
    @Mapping(target = "user.id", source = "userInfo.id")
    @Mapping(target = "user.firstName", source = "userInfo.firstName")
    @Mapping(target = "user.lastName", source = "userInfo.lastName")
    @Mapping(target = "ratedByUser.id", source = "ratedByUserInfo.id")
    @Mapping(target = "ratedByUser.firstName", source = "ratedByUserInfo.firstName")
    @Mapping(target = "ratedByUser.lastName", source = "ratedByUserInfo.lastName")
    @Mapping(target = "id", source = "rating.id")
    @BeanMapping(qualifiedByName = "addLinks")
    RatingResponse toResponse(Rating rating, UserInfo userInfo, UserInfo ratedByUserInfo);

    @AfterMapping
    @Named("addLinks")
    default RatingResponse addLinks(Rating rating, @MappingTarget RatingResponse response) {

        response.add(Link.of("/ratings/" + rating.getId()).withSelfRel());

        return response;
    }
}
