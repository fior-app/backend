package app.fior.backend.data

import app.fior.backend.model.Group
import app.fior.backend.model.GroupMember
import app.fior.backend.model.UserCompact
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface GroupMemberRepository : ReactiveMongoRepository<GroupMember, String> {

    fun findAllByMember(member: UserCompact): Flux<GroupMember>

    fun findAllByMemberEmailAndState(memberEmail: String, state: GroupMember.GroupMemberState): Flux<GroupMember>

    fun findAllByMemberAndState(member: UserCompact, state: GroupMember.GroupMemberState): Flux<GroupMember>

    fun findByGroupAndMember(group: Group, member: UserCompact): Mono<GroupMember>

    fun findByGroupAndMember_Email(group: Group, member_email: String): Mono<GroupMember>

    fun findByGroup(group: Group): Flux<GroupMember>

    fun findByGroupAndState(group: Group, state: GroupMember.GroupMemberState): Flux<GroupMember>
}