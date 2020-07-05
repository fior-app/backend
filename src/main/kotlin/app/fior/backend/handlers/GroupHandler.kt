package app.fior.backend.handlers

import app.fior.backend.data.ChatroomRepository
import app.fior.backend.data.GroupMemberRepository
import app.fior.backend.data.GroupRepository
import app.fior.backend.data.UserRepository
import app.fior.backend.dto.GroupCreateRequest
import app.fior.backend.dto.GroupStateChangeRequest
import app.fior.backend.dto.MemberAddRequest
import app.fior.backend.extensions.*
import app.fior.backend.model.Group
import app.fior.backend.model.GroupMember
import app.fior.backend.model.commiunication.Chatroom
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class GroupHandler(
        private val userRepository: UserRepository,
        private val groupRepository: GroupRepository,
        private val groupMemberRepository: GroupMemberRepository,
        private val chatroomRepository: ChatroomRepository
) {

    fun createGroup(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(GroupCreateRequest::class.java)
    ).flatMap { (user, groupRequest) ->
        chatroomRepository.save(Chatroom(groupRequest.name, Chatroom.ChatroomType.GROUP))
                .flatMap { chatroom ->
                    groupRepository.save(
                            Group(
                                    groupRequest.name,
                                    groupRequest.description,
                                    groupRequest.icon,
                                    user.compact(),
                                    chatroom.compact()
                            )
                    ).flatMap { group ->
                        groupMemberRepository.save(
                                GroupMember(
                                        group,
                                        user.compact(),
                                        GroupMember.GroupMemberState.OK
                                ).withAllPermissions()
                        ).flatMap {
                            "Group Created Successfully".toSuccessServerResponse()
                        }
                    }
                }
    }

    fun getGroup(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            groupRepository.findById(request.pathVariable("groupId"))
    ).flatMap { (user, group) ->
        groupMemberRepository.findByGroupAndMember(group, user.compact())
                .flatMap {
                    ServerResponse.ok().bodyValue(group)
                }.switchIfEmpty {
                    "you are not a member in the group".toForbiddenServerResponse()
                }
    }.switchIfEmpty {
        "group not found".toNotFoundServerResponse()
    }

    fun getGroupMembers(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            groupRepository.findById(request.pathVariable("groupId"))
    ).flatMap { (user, group) ->
        groupMemberRepository.findByGroupAndMember(group, user.compact())
                .flatMap {
                    ServerResponse.ok().body(
                            groupMemberRepository.findByGroup(group)
                                    .skip(request.queryParam("skip").orElse("0").toLong())
                                    .take(request.queryParam("limit").orElse("25").toLong()),
                            GroupMember::class.java
                    )
                }.switchIfEmpty {
                    "you are not a member in the group".toForbiddenServerResponse()
                }
    }.switchIfEmpty {
        "group not found".toNotFoundServerResponse()
    }

    fun groupsMe(request: ServerRequest) = request.principalUser(userRepository)
            .flatMap { user ->
                ServerResponse.ok().body(
                        groupMemberRepository.findAllByMemberAndState(user.compact(), GroupMember.GroupMemberState.OK)
                                .skip(request.queryParam("skip").orElse("0").toLong())
                                .take(request.queryParam("limit").orElse("25").toLong()),
                        GroupMember::class.java
                )
            }

    fun groupsMeAll(request: ServerRequest) = request.principalUser(userRepository)
            .flatMap { user ->
                ServerResponse.ok().body(
                        groupMemberRepository.findAllByMember(user.compact())
                                .skip(request.queryParam("skip").orElse("0").toLong())
                                .take(request.queryParam("limit").orElse("25").toLong()),
                        GroupMember::class.java
                )
            }


    fun groupsMeRequests(request: ServerRequest) = request.principalUser(userRepository)
            .flatMap { user ->
                ServerResponse.ok().body(
                        groupMemberRepository.findAllByMemberAndState(user.compact(), GroupMember.GroupMemberState.CONFIRM)
                                .skip(request.queryParam("skip").orElse("0").toLong())
                                .take(request.queryParam("limit").orElse("25").toLong()),
                        GroupMember::class.java
                )
            }

    fun requestMemberToGroup(request: ServerRequest) = Mono.zip(
            request.bodyToMono(MemberAddRequest::class.java),
            request.principalUser(userRepository),
            groupRepository.findById(request.pathVariable("groupId"))
    ).flatMap { (memberAddRequest, user, group) ->
        Mono.zip(
                groupMemberRepository.findByGroupAndMember(group, user.compact()),
                userRepository.findById(memberAddRequest.memberId)
        ).flatMap member@{ (userMember, member) ->
            if (!userMember.hasPermission(GroupMember.Permission.SEND_MEMBER_REQUESTS))
                return@member "Unauthorized to send requests".toUnauthorizedServerResponse()
            groupMemberRepository.findByGroupAndMember(group, member.compact())
                    .flatMap { _ ->
                        "User is already a member".toForbiddenServerResponse()
                    }.switchIfEmpty {
                        groupMemberRepository.save(
                                GroupMember(
                                        group,
                                        member.compact(),
                                        GroupMember.GroupMemberState.CONFIRM
                                )
                        ).flatMap {
                            "User requested to group".toSuccessServerResponse()
                        }
                    }
        }.switchIfEmpty {
            "User or group not found".toNotFoundServerResponse()
        }
    }

    fun leaveGroup(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            groupRepository.findById(request.pathVariable("roomId"))
    ).flatMap { (user, group) ->
        groupMemberRepository.findByGroupAndMember(group, user.compact())
                .flatMap { groupMember ->
                    Mono.zip(
                            groupMemberRepository.delete(groupMember),
                            groupRepository.save(group.minusMember())
                    )
                            .flatMap {
                                "Group Leave successfully!".toSuccessServerResponse()
                            }
                }.switchIfEmpty {
                    "User are not a member in group".toForbiddenServerResponse()
                }
    }.switchIfEmpty {
        "Group not found".toNotFoundServerResponse()
    }

    fun changeGroupState(request: ServerRequest) = Mono.zip(
            request.principalUser(userRepository),
            request.bodyToMono(GroupStateChangeRequest::class.java),
            groupRepository.findById(request.pathVariable("roomId"))
    ).flatMap { (user, stateChangeRequest, group) ->
        groupMemberRepository.findByGroupAndMember(group, user.compact())
                .flatMap { groupMember ->
                    groupMember.state = stateChangeRequest.state
                    if (stateChangeRequest.state == GroupMember.GroupMemberState.CONFIRM) {
                        groupRepository.save(group.plusMember())
                    }
                    groupMemberRepository.save(groupMember)
                            .flatMap {
                                "Group State changed!".toSuccessServerResponse()
                            }
                }.switchIfEmpty {
                    "User is not a member in group".toForbiddenServerResponse()
                }
    }.switchIfEmpty {
        "Group not found".toNotFoundServerResponse()
    }
}
