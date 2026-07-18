package online.storytime.creators.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import online.storytime.creators.core.network.AppConfig

// MARK: - User

@Serializable
data class CreatorUser(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val image: String? = null,
    val role: String? = null,
    val activeRole: String? = null,
    val bio: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val website: String? = null,
    val networkHandle: String? = null,
    val professionalName: String? = null,
    val phoneNumber: String? = null,
    val platformRoles: List<String>? = null,
    val reputationScore: Double? = null,
    val multiRole: Boolean? = null,
) {
    val displayName: String
        get() {
            professionalName?.takeIf { it.isNotEmpty() }?.let { return it }
            name?.takeIf { it.isNotEmpty() }?.let { return it }
            networkHandle?.takeIf { it.isNotEmpty() }?.let { return "@$it" }
            return email ?: "Creator"
        }

    val effectiveRole: String get() = activeRole ?: role ?: ""

    val isCreatorPortalEligible: Boolean
        get() {
            val roles = ((platformRoles ?: emptyList()) + listOf(effectiveRole).filter { it.isNotEmpty() }).toSet()
            return roles.contains(AppConfig.CREATOR_ROLE)
        }
}

// MARK: - Projects

@Serializable
data class ProjectsResponse(
    val projects: List<CreatorProject> = emptyList(),
    val meId: String? = null,
)

@Serializable
data class CreatorProject(
    val id: String,
    val title: String = "Untitled",
    val logline: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val status: String? = null,
    val phase: String? = null,
    val budget: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val ideasCount: Int? = null,
    val isOriginal: Boolean? = null,
    val pipelineRollup: PipelineRollup? = null,
    val projectToolProgress: List<ToolProgress>? = null,
) {
    val phaseLabel: String
        get() = when ((phase ?: status ?: "").uppercase()) {
            "PRE_PRODUCTION" -> "Pre-Production"
            "PRODUCTION" -> "Production"
            "POST_PRODUCTION" -> "Post-Production"
            else -> phase ?: status ?: "Project"
        }
}

@Serializable
data class PipelineRollup(
    val activeStep: Int? = null,
    val totalTracked: Int? = null,
    val completeCount: Int? = null,
    val inProgressCount: Int? = null,
    val skippedCount: Int? = null,
    val notStartedCount: Int? = null,
    val progressPercent: Double? = null,
    val phaseSummaries: PipelinePhaseSummaries? = null,
) {
    val overallPercent: Double? get() = progressPercent
    val completedTools: Int?
        get() {
            if (completeCount == null && skippedCount == null) return null
            return (completeCount ?: 0) + (skippedCount ?: 0)
        }
    val totalTools: Int? get() = totalTracked
    val inProgress: Int get() = inProgressCount ?: 0
}

@Serializable
data class PipelinePhaseSummaries(
    val pre: PipelinePhaseSummary? = null,
    val prod: PipelinePhaseSummary? = null,
    val post: PipelinePhaseSummary? = null,
)

@Serializable
data class PipelinePhaseSummary(
    val done: Int? = null,
    val skipped: Int? = null,
    val inProgress: Int? = null,
    val total: Int? = null,
)

@Serializable
data class ToolProgress(
    val toolId: String,
    val phase: String? = null,
    val status: String? = null,
    val percent: Double? = null,
)

// MARK: - Calendar / catalogue simple

@Serializable
data class CalendarEvent(
    val id: String,
    val title: String,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val projectId: String? = null,
    val notes: String? = null,
)

@Serializable
data class ContentListResponse(
    val content: List<CatalogueItem>? = null,
    val items: List<CatalogueItem>? = null,
) {
    val all: List<CatalogueItem> get() = content ?: items ?: emptyList()
}

@Serializable
data class CatalogueItem(
    val id: String,
    val title: String,
    val type: String? = null,
    val reviewStatus: String? = null,
    val thumbnailUrl: String? = null,
    val posterUrl: String? = null,
    val createdAt: String? = null,
)

// MARK: - Messages (simple)

@Serializable
data class MessagesResponse(
    val threads: List<MessageThread>? = null,
    val messages: List<ChatMessage>? = null,
)

@Serializable
data class MessageThread(
    val id: String,
    val subject: String? = null,
    val preview: String? = null,
    val updatedAt: String? = null,
    val unreadCount: Int? = null,
    val counterpartName: String? = null,
)

@Serializable
data class ChatMessage(
    val id: String,
    val body: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
) {
    val text: String get() = body ?: content ?: ""
}

// MARK: - Roster / marketplace listings

@Serializable
data class CastRosterResponse(
    val items: List<RosterContact>? = null,
    val roster: List<RosterContact>? = null,
    val contacts: List<RosterContact>? = null,
) {
    val all: List<RosterContact> get() = items ?: roster ?: contacts ?: emptyList()
}

@Serializable
data class RosterContact(
    val id: String,
    val name: String = "Contact",
    @JsonNames("roleType") val role: String? = null,
    @JsonNames("contactEmail") val email: String? = null,
    val phone: String? = null,
    val notes: String? = null,
    val agency: String? = null,
    val department: String? = null,
    val pastWork: String? = null,
    val pastProjects: String? = null,
)

@Serializable
data class LocationListResponse(
    val locations: List<LocationListing>? = null,
    val items: List<LocationListing>? = null,
) {
    val all: List<LocationListing> get() = locations ?: items ?: emptyList()
}

@Serializable
data class LocationListing(
    val id: String,
    val name: String = "Location",
    val type: String? = null,
    val city: String? = null,
    val dailyRate: Double? = null,
    val description: String? = null,
    val photoUrls: List<String>? = null,
)

@Serializable
data class CrewTeamsResponse(
    val teams: List<CrewTeam>? = null,
    val items: List<CrewTeam>? = null,
) {
    val all: List<CrewTeam> get() = teams ?: items ?: emptyList()
}

@Serializable
data class CrewTeam(
    val id: String,
    @JsonNames("companyName") val name: String = "Crew Team",
    @JsonNames("specializations") val specialty: String? = null,
    @SerialName("location") val locationRaw: String? = null,
    val city: String? = null,
    val country: String? = null,
    val description: String? = null,
) {
    val location: String?
        get() = locationRaw ?: listOfNotNull(city, country).joinToString(", ").ifEmpty { null }
}

@Serializable
data class CastingAgenciesResponse(
    val agencies: List<CastingAgency>? = null,
    val items: List<CastingAgency>? = null,
) {
    val all: List<CastingAgency> get() = agencies ?: items ?: emptyList()
}

@Serializable
data class CastingAgency(
    val id: String,
    @JsonNames("agencyName") val name: String = "Agency",
    @SerialName("location") val locationRaw: String? = null,
    val city: String? = null,
    val country: String? = null,
    val description: String? = null,
) {
    val location: String?
        get() = locationRaw ?: listOfNotNull(city, country).joinToString(", ").ifEmpty { null }
}

@Serializable
data class EquipmentListResponse(
    val equipment: List<EquipmentItem>? = null,
    val items: List<EquipmentItem>? = null,
) {
    val all: List<EquipmentItem> get() = equipment ?: items ?: emptyList()
}

@Serializable
data class EquipmentItem(
    val id: String,
    @JsonNames("companyName") val name: String = "Equipment",
    val category: String? = null,
    val dailyRate: Double? = null,
    val description: String? = null,
    val location: String? = null,
)

@Serializable
data class CateringListResponse(
    val companies: List<CateringCompany>? = null,
    val items: List<CateringCompany>? = null,
) {
    val all: List<CateringCompany> get() = companies ?: items ?: emptyList()
}

@Serializable
data class CateringCompany(
    val id: String,
    @JsonNames("companyName") val name: String = "Catering",
    @JsonNames("specializations") val cuisine: String? = null,
    @SerialName("location") val locationRaw: String? = null,
    val city: String? = null,
    val country: String? = null,
    val description: String? = null,
) {
    val location: String?
        get() = locationRaw ?: listOfNotNull(city, country).joinToString(", ").ifEmpty { null }
}

// MARK: - Project tool payloads

@Serializable
data class CastingRolesResponse(val roles: List<CastingRole>? = null)

@Serializable
data class CastingRole(
    val id: String,
    val name: String = "Role",
    val importance: String? = null,
    val status: String? = null,
    val description: String? = null,
    val dailyRate: Double? = null,
)

// MARK: - MODOC

@Serializable
data class ModocStatus(
    val available: Boolean? = null,
    val provider: String? = null,
    val defaultModel: String? = null,
)

@Serializable
data class ModocContext(
    val greeting: String? = null,
    val suggestions: List<String>? = null,
    val unreadCount: Int? = null,
)

// MARK: - Music

@Serializable
data class MusicTrack(
    val id: String,
    val title: String = "Track",
    val artistName: String? = null,
    val genre: String? = null,
    val mood: String? = null,
    val duration: Int? = null,
    val description: String? = null,
    val licenseType: String? = null,
)

@Serializable
data class MusicSelectionResponse(val selection: MusicSelection? = null)

@Serializable
data class MusicSelection(
    val id: String,
    val usage: String? = null,
    val notes: String? = null,
    val track: MusicTrack? = null,
)

// MARK: - Legal inbox

@Serializable
data class LegalInboxItem(
    val id: String,
    val projectId: String = "",
    val projectTitle: String = "",
    val title: String = "Contract",
    val status: String? = null,
    val statusLabel: String? = null,
    val senderName: String? = null,
    val requiredAction: String? = null,
    val signatureDeadline: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class LegalInboxBuckets(
    val waitingForYou: List<LegalInboxItem>? = null,
    val pending: List<LegalInboxItem>? = null,
    val completed: List<LegalInboxItem>? = null,
)

@Serializable
data class LegalInboxResponse(val buckets: LegalInboxBuckets? = null)

// MARK: - Generic responses

@Serializable
data class IdResponse(val id: String = "")

@Serializable
data class OkResponse(val ok: Boolean? = null)

@Serializable
data class FollowResponse(val following: Boolean? = null)

@Serializable
data class CalendarEventsResponse(val events: List<CalendarEvent>? = null)

// MARK: - Request bodies

@Serializable
data class CreateProjectBody(
    val title: String,
    val logline: String? = null,
    val type: String? = null,
    val genre: String? = null,
)

@Serializable
data class CreateProjectResponse(val project: CreatorProject)

@Serializable
data class InquiryBody(
    val message: String,
    val projectId: String? = null,
    val agencyId: String? = null,
    val teamId: String? = null,
    val locationId: String? = null,
    val equipmentId: String? = null,
    val cateringCompanyId: String? = null,
)

@Serializable
data class ChatPostBody(
    val messages: List<ChatUIMessage>,
    val scope: String? = null,
    val pageContext: Map<String, String>? = null,
    val conversationId: String? = null,
)

@Serializable
data class ChatUIMessage(
    val role: String,
    val content: String,
)

@Serializable
data class CreateCastRosterBody(
    val name: String,
    val roleType: String? = null,
    val contactEmail: String? = null,
    val notes: String? = null,
    val pastWork: String? = null,
)

@Serializable
data class UpdateCastRosterBody(
    val name: String? = null,
    val roleType: String? = null,
    val contactEmail: String? = null,
    val notes: String? = null,
    val pastWork: String? = null,
)

@Serializable
data class CreateCrewRosterBody(
    val name: String,
    val role: String? = null,
    val department: String? = null,
    val contactEmail: String? = null,
    val phone: String? = null,
    val notes: String? = null,
    val pastProjects: String? = null,
)

@Serializable
data class CastInquiryBody(
    val agencyId: String,
    val projectName: String? = null,
    val roleName: String? = null,
    val message: String? = null,
    val talentId: String? = null,
    val projectId: String? = null,
)

@Serializable
data class CrewTeamRequestBody(
    val crewTeamId: String,
    val projectName: String? = null,
    val message: String? = null,
    val projectId: String? = null,
)

@Serializable
data class LocationBookingBody(
    val locationId: String,
    val note: String? = null,
    val shootType: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val crewSize: Int? = null,
    val projectId: String? = null,
    val projectTitle: String? = null,
)

@Serializable
data class EquipmentRequestBody(
    val equipmentId: String,
    val note: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val projectId: String? = null,
    val projectTitle: String? = null,
)

@Serializable
data class CateringBookingBody(
    val cateringCompanyId: String,
    val eventDate: String? = null,
    val headCount: Int? = null,
    val note: String? = null,
    val projectId: String? = null,
    val projectTitle: String? = null,
)

@Serializable
data class MusicSelectionBody(
    val trackId: String,
    val usage: String? = null,
    val notes: String? = null,
)

@Serializable
data class ToolProgressBody(
    val phase: String,
    val toolId: String,
    val status: String,
    val percent: Double,
)

@Serializable
data class AccountPatchBody(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val website: String? = null,
    val networkHandle: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null,
)

@Serializable
data class BodyOnly(val body: String)

@Serializable
data class ActionBody(val action: String)

@Serializable
data class CsrfResponse(val csrfToken: String)
