package com.stepaniuk.testhorizon.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.post.PostCreateRequest;
import com.stepaniuk.testhorizon.payload.post.PostResponse;
import com.stepaniuk.testhorizon.payload.post.PostUpdateRequest;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostByIdException;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostCategoryByNameException;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.testspecific.jwt.WithJwtToken;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @MockitoBean
    private PostService postService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PageMapper pageMapper;

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPostResponseWhenCreatingPost() throws Exception {
        // given
        Long userId = 1L;

        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "Title",
                "Content",
                "Description",
                PostCategoryName.QUALITY_ASSURANCE);

        PostResponse postResponse = createPostResponse();

        // when
        when(postService.createPost(eq(postCreateRequest), eq(userId), any())).thenReturn(postResponse);

        // then
        mockMvc.perform(post("/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(postResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(postResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(postResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(postResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(postResponse.getDescription())))
                .andExpect(jsonPath("$.content", is(postResponse.getContent())))
                .andExpect(jsonPath("$.category", is(postResponse.getCategory().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(postResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(postResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenCreatingPost() throws Exception {
        // given
        Long userId = 1L;

        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "Title",
                "Content",
                "Description",
                PostCategoryName.QUALITY_ASSURANCE);

        when(postService.createPost(eq(postCreateRequest), eq(userId), any())).thenThrow(
                new NoSuchPostCategoryByNameException(PostCategoryName.QUALITY_ASSURANCE)
        );

        // when
        mockMvc.perform(post("/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such post category")))
                .andExpect(jsonPath("$.detail", is("No post category with name " + PostCategoryName.QUALITY_ASSURANCE)))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPostResponseWhenGettingById() throws Exception {
        // given
        Long postId = 1L;

        PostResponse postResponse = createPostResponse();

        // when
        when(postService.getPostById(postId)).thenReturn(postResponse);

        // then
        mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(postResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(postResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(postResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(postResponse.getDescription())))
                .andExpect(jsonPath("$.content", is(postResponse.getContent())))
                .andExpect(jsonPath("$.category", is(postResponse.getCategory().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(postResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(postResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGettingById() throws Exception {
        // given
        Long postId = 1L;

        when(postService.getPostById(postId)).thenThrow(
                new NoSuchPostByIdException(postId)
        );

        // when
        mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such post")))
                .andExpect(jsonPath("$.detail", is("No post with id " + postId)))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPostResponseWhenUpdatingPost() throws Exception {
        // given
        Long postId = 1L;

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest(
                "New Title", null, null, null);

        PostResponse postResponse = createPostResponse();

        // when
        when(postService.updatePost(eq(postId), eq(postUpdateRequest), any(), any())).thenReturn(postResponse);

        // then
        mockMvc.perform(patch("/posts/" + postId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(postResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(postResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(postResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(postResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(postResponse.getDescription())))
                .andExpect(jsonPath("$.content", is(postResponse.getContent())))
                .andExpect(jsonPath("$.category", is(postResponse.getCategory().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(postResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(postResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchPostByIdExceptionWhenUpdatingPost() throws Exception {
        // given
        Long postId = 1L;

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest(
                "New Title", null, null, null);

        when(postService.updatePost(eq(postId), eq(postUpdateRequest), any(), any())).thenThrow(
                new NoSuchPostByIdException(postId)
        );

        // when
        mockMvc.perform(patch("/posts/" + postId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such post")))
                .andExpect(jsonPath("$.detail", is("No post with id " + postId)))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchPostCategoryByNameExceptionWhenUpdatingPost() throws Exception {
        // given
        Long postId = 1L;

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest(
                null, null, null, PostCategoryName.QUALITY_ASSURANCE);

        when(postService.updatePost(eq(postId), eq(postUpdateRequest), any(), any())).thenThrow(
                new NoSuchPostCategoryByNameException(postUpdateRequest.getCategory())
        );

        // when
        mockMvc.perform(patch("/posts/" + postId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such post category")))
                .andExpect(jsonPath("$.detail", is("No post category with name " + postUpdateRequest.getCategory())))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingPost() throws Exception{
        // given
        Long postId = 1L;

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest(
                null, null, null, PostCategoryName.QUALITY_ASSURANCE);

        doThrow(new AccessToManageEntityDeniedException("Post", "/posts"))
                .when(postService)
                .updatePost(eq(postId), eq(postUpdateRequest), any(), any());

        // when
        mockMvc.perform(patch("/posts/" + postId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Post denied")))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingPost() throws Exception {
        // given
        long postId = 1L;

        // when && then
        mockMvc.perform(delete("/posts/" + postId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchPostByIdExceptionWhenDeletingPost() throws Exception {
        // given
        Long postId = 1L;

        doThrow(new NoSuchPostByIdException(postId)).
                when(postService).
                deletePostById(eq(postId), any(), any());

        // when
        mockMvc.perform(delete("/posts/" + postId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such post")))
                .andExpect(jsonPath("$.detail", is("No post with id " + postId)))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingPost() throws Exception {
        // given
        Long postId = 1L;

        doThrow(new AccessToManageEntityDeniedException("Post", "/posts"))
                .when(postService)
                .deletePostById(eq(postId), any(), any());

        // when
        mockMvc.perform(delete("/posts/" + postId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Post denied")))
                .andExpect(jsonPath("$.instance", is("/posts")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfPostResponsesWhenGettingAllPosts() throws Exception {
        // given
        var response = createPostResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(postService.getAllPosts(pageable, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/posts"))
                );

        // then
        mockMvc.perform(get("/posts")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.posts[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.posts[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.posts[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.posts[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.posts[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.posts[0].category", is(response.getCategory().name())))
                .andExpect(jsonPath("$._embedded.posts[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0]._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfPostResponsesWhenGettingAllPostsByOwnerId() throws Exception {
        // given
        var response = createPostResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(postService.getAllPosts(pageable, 1L, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/posts"))
                );

        // then
        mockMvc.perform(get("/posts")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("ownerId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.posts[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.posts[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.posts[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.posts[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.posts[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.posts[0].category", is(response.getCategory().name())))
                .andExpect(jsonPath("$._embedded.posts[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0]._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfPostResponsesWhenGettingAllPostsByTitle() throws Exception {
        // given
        var response = createPostResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(postService.getAllPosts(pageable, null, "title", null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/posts"))
                );

        // then
        mockMvc.perform(get("/posts")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("title", "title")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.posts[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.posts[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.posts[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.posts[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.posts[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.posts[0].category", is(response.getCategory().name())))
                .andExpect(jsonPath("$._embedded.posts[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0]._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.delete.href", is("http://localhost/posts/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfPostResponsesWhenGettingAllPostsByCategory() throws Exception {
        // given
        var response = createPostResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(postService.getAllPosts(pageable, null, null, PostCategoryName.QUALITY_ASSURANCE))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/posts"))
                );

        // then
        mockMvc.perform(get("/posts")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("category", PostCategoryName.QUALITY_ASSURANCE.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.posts[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.posts[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.posts[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.posts[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.posts[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.posts[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.posts[0].category", is(response.getCategory().name())))
                .andExpect(jsonPath("$._embedded.posts[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.posts[0]._links.self.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.update.href", is("http://localhost/posts/1")))
                .andExpect(jsonPath("$._embedded.posts[0]._links.delete.href", is("http://localhost/posts/1")));
    }

    private PostResponse createPostResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new PostResponse(
                1L,
                new UserInfo(1L, "Name", "Surname"),
                "Title",
                "Content",
                "Description",
                PostCategoryName.QUALITY_ASSURANCE,
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/posts/1", "self"));
        response.add(Link.of("http://localhost/posts/1", "update"));
        response.add(Link.of("http://localhost/posts/1", "delete"));

        return response;
    }

}
