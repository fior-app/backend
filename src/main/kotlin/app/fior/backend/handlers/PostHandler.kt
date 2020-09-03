package app.fior.backend.handlers

import app.fior.backend.data.*
import app.fior.backend.dto.*
import app.fior.backend.extensions.*
import app.fior.backend.model.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux


@Component
class PostHandler(
        private val postRepository: PostRepository,
        private val skillRepository: SkillRepository,
        private val userRepository: UserRepository
) {

    fun getPosts(request: ServerRequest) = ServerResponse.ok().body(
            postRepository.findAll()
                    .skip(request.queryParam("skip").orElse("0").toLong())
                    .take(request.queryParam("limit").orElse("25").toLong()),
            Post::class.java
    )

    fun getMyPosts(request: ServerRequest) = request.principalUser(userRepository).flatMap {
        ServerResponse.ok().body(
                postRepository.findAllByCreatedBy_Id(it.id!!)
                        .skip(request.queryParam("skip").orElse("0").toLong())
                        .take(request.queryParam("limit").orElse("25").toLong()),
                Post::class.java
        )
    }

    fun getPost(request: ServerRequest) = postRepository.findById(request.pathVariable("postId"))
            .flatMap {
                ServerResponse.ok().bodyValue(it)
            }.switchIfEmpty {
                "Post not found".toNotFoundServerResponse()
            }

    fun createPost(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(PostCreateRequest::class.java)
    ).flatMap { (user, post) ->
        post.skills.toFlux().flatMap {
            skillRepository.findById(it)
        }.collectList().flatMap { skills ->
            postRepository.save(Post(
                    post,
                    skills.map { SkillCompact(it.id!!, it.name) },
                    user.compact()
            )).flatMap {
                "Post created successfully".toSuccessServerResponse()
            }
        }
    }

    fun updatePost(request: ServerRequest) = postRepository.findById(request.pathVariable("postId"))
            .flatMap { post ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (post.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    request.bodyToMono(PostUpdateRequest::class.java).flatMap { updateRequest ->
                        (updateRequest.skills ?: listOf()).toFlux().flatMap {
                            skillRepository.findById(it)
                        }.collectList().flatMap { skills ->
                            postRepository.save(post.updated(
                                    updateRequest,
                                    if (skills.isEmpty()) null else skills.map { SkillCompact(it.id!!, it.name) }
                            )).flatMap {
                                "Post updated successfully".toSuccessServerResponse()
                            }
                        }
                    }
                }
            }.switchIfEmpty {
                "Post not found".toNotFoundServerResponse()
            }

    fun deletePost(request: ServerRequest) = postRepository.findById(request.pathVariable("postId"))
            .flatMap { post ->
                request.principalUser(userRepository).flatMap principal@{ user ->
                    if (post.createdBy.id != user.id)
                        return@principal "Unauthorized".toUnauthorizedServerResponse()

                    postRepository.deleteById(post.id!!).thenReturn(true).flatMap {
                        "Post deleted successfully".toSuccessServerResponse()
                    }
                }
            }.switchIfEmpty {
                "Post not found".toNotFoundServerResponse()
            }

}
