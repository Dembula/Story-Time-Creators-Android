package online.storytime.creators.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

// MARK: - Command Center

@Serializable
data class CommandCenterAPIResponse(
    val analytics: CreatorAnalyticsPayload? = null,
    val overview: CommandCenterOverview? = null,
    val production: CommandCenterProduction? = null,
    val ai: CommandCenterAI? = null,
    val retention: RetentionSnapshot? = null,
)

@Serializable
data class CommandCenterOverview(
    val activeProjects: Int? = null,
    val topFilmTitle: String? = null,
    val topFilmViews: Int? = null,
    val topFilmRevenueRand: Double? = null,
    val viewerGrowth7dPct: Double? = null,
    val engagementRateApprox: Double? = null,
    val viewsLast7d: Int? = null,
    val viewsPrev7d: Int? = null,
)

@Serializable
data class CommandCenterProduction(
    val shootDaysTotal: Int? = null,
    val openIncidents: Int? = null,
    val callSheetsSaved: Int? = null,
    val tasksByStatus: Map<String, Int>? = null,
)

@Serializable
data class CommandCenterAI(
    val modocConversationsInRange: Int? = null,
    val modocUserMessagesInRange: Int? = null,
    val topTasks: List<ModocTaskCount>? = null,
)

@Serializable
data class ModocTaskCount(
    val task: String,
    val count: Int,
)

@Serializable
data class CreatorAnalyticsPayload(
    val rangeKey: String? = null,
    val period: AnalyticsPeriod? = null,
    val revenue: AnalyticsRevenue? = null,
    val engagement: AnalyticsEngagement? = null,
    val contentPerformance: List<ContentPerformanceRow>? = null,
    val projects: AnalyticsProjects? = null,
    val competition: AnalyticsCompetition? = null,
)

@Serializable
data class AnalyticsPeriod(val start: String? = null, val end: String? = null)

@Serializable
data class AnalyticsRevenue(
    val amount: Double? = null,
    val watchTimeSeconds: Double? = null,
    val sharePercent: Double? = null,
    val totalViews: Int? = null,
    val streamCount: Int? = null,
    val perViewRand: Double? = null,
    val perStreamRand: Double? = null,
    val creatorPool: Double? = null,
    val viewerSubRevenue: Double? = null,
)

@Serializable
data class AnalyticsEngagement(
    val totalViews: Int? = null,
    val uniqueWatchers: Int? = null,
    val averageWatchTimeSeconds: Double? = null,
    val totalWatchTimeSeconds: Double? = null,
    val totalComments: Int? = null,
    val totalRatings: Int? = null,
    val watchlistCount: Int? = null,
    val contentCount: Int? = null,
)

@Serializable
data class ContentPerformanceRow(
    val id: String,
    val title: String = "Untitled",
    val type: String? = null,
    val reviewStatus: String? = null,
    val seasonCount: Int? = null,
    val views: Int? = null,
    val watchTimeSeconds: Double? = null,
    val comments: Int? = null,
    val ratings: Int? = null,
    val watchlistAdds: Int? = null,
    val avgRating: Double? = null,
)

@Serializable
data class AnalyticsProjects(
    val total: Int? = null,
    val byPhase: Map<String, Int>? = null,
    val byStatus: Map<String, Int>? = null,
)

@Serializable
data class AnalyticsCompetition(
    val periodName: String? = null,
    val endDate: String? = null,
    val rank: Int? = null,
    val voteCount: Int? = null,
)

@Serializable
data class RetentionSnapshot(
    val sampleSize: Int? = null,
    val curve: List<RetentionPoint>? = null,
    val byTitle: List<RetentionByTitle>? = null,
)

@Serializable
data class RetentionPoint(val checkpoint: Int, val retainedPct: Double)

@Serializable
data class RetentionByTitle(
    val contentId: String,
    val title: String = "",
    val sampleSize: Int? = null,
    val medianCompletionPct: Double? = null,
    val curve: List<RetentionPoint>? = null,
)

@Serializable
data class CommandCenterCalendarPayload(
    val events: List<CommandCenterCalendarEvent>? = null,
    val teamMembers: List<CalendarTeamMember>? = null,
    val companyId: String? = null,
    val companyName: String? = null,
    val isCompanyAccount: Boolean? = null,
    val projects: List<CalendarProjectRef>? = null,
    val rangeStart: String? = null,
    val rangeEnd: String? = null,
)

@Serializable
data class CommandCenterCalendarEvent(
    val id: String,
    val kind: String? = null,
    val title: String = "Event",
    val description: String? = null,
    val startAt: String = "",
    val endAt: String? = null,
    val allDay: Boolean? = null,
    val projectId: String? = null,
    val projectTitle: String? = null,
    val href: String? = null,
    val editable: Boolean? = null,
    val visibility: String? = null,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val createdById: String? = null,
    val status: String? = null,
)

@Serializable
data class CalendarTeamMember(
    val userId: String,
    val name: String = "",
    val email: String? = null,
    val profileDisplayName: String? = null,
)

@Serializable
data class CalendarProjectRef(val id: String, val title: String = "")

@Serializable
data class CreateCalendarEventBody(
    val title: String,
    val description: String? = null,
    val startAt: String,
    val endAt: String? = null,
    val allDay: Boolean,
    val visibility: String,
    val projectId: String? = null,
    val assigneeId: String? = null,
)

@Serializable
data class UpdateCalendarEventBody(
    val title: String? = null,
    val description: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val allDay: Boolean? = null,
    val visibility: String? = null,
    val projectId: String? = null,
    val assigneeId: String? = null,
)

// MARK: - Network

@Serializable
data class NetworkPostsResponse(val posts: List<EnrichedNetworkPost>? = null)

@Serializable
data class EnrichedNetworkPost(
    val id: String,
    val authorId: String? = null,
    val body: String? = null,
    val imageUrls: String? = null,
    val videoUrls: String? = null,
    val contentId: String? = null,
    val projectId: String? = null,
    val postType: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val author: NetworkAuthor? = null,
    val content: NetworkPostContentRef? = null,
    val project: NetworkPostProjectRef? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val saveCount: Int? = null,
    val likedByViewer: Boolean? = null,
    val savedByViewer: Boolean? = null,
)

@Serializable
data class NetworkAuthor(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val networkHandle: String? = null,
    val handle: String? = null,
    val displayName: String? = null,
    val image: String? = null,
    val headline: String? = null,
    val primaryRole: String? = null,
    val professionalName: String? = null,
) {
    val label: String
        get() = displayName ?: professionalName ?: name ?: handle?.let { "@$it" } ?: "Creator"
}

@Serializable
data class NetworkPostContentRef(
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val posterUrl: String? = null,
)

@Serializable
data class NetworkPostProjectRef(
    val id: String? = null,
    val title: String? = null,
    val type: String? = null,
    val phase: String? = null,
    val status: String? = null,
)

@Serializable
data class NetworkCreatorsResponse(val creators: List<DiscoverCreator>? = null)

@Serializable
data class DiscoverCreator(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val networkHandle: String? = null,
    val image: String? = null,
    val bio: String? = null,
    val role: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val handle: String? = null,
    val displayName: String? = null,
    val following: Boolean? = null,
    val connectionStatus: String? = null,
    val followerCount: Int? = null,
) {
    val label: String get() = displayName ?: name ?: handle?.let { "@$it" } ?: "Creator"
}

@Serializable
data class NetworkConnectionsResponse(
    val received: List<ConnectionRequestRow>? = null,
    val sent: List<ConnectionRequestRow>? = null,
)

@Serializable
data class ConnectionRequestRow(
    val id: String,
    val fromId: String? = null,
    val toId: String? = null,
    val status: String? = null,
    val message: String? = null,
    val createdAt: String? = null,
    val respondedAt: String? = null,
    val from: NetworkAuthor? = null,
    val to: NetworkAuthor? = null,
)

@Serializable
data class NetworkProfileResponse(
    val user: NetworkProfileUser? = null,
    val following: Boolean? = null,
    val connectionStatus: String? = null,
    val followerCount: Int? = null,
    val followingCount: Int? = null,
    val contents: List<NetworkProfileContent>? = null,
    val posts: List<EnrichedNetworkPost>? = null,
)

@Serializable
data class NetworkProfileUser(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val networkHandle: String? = null,
    val image: String? = null,
    val bio: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val website: String? = null,
    val role: String? = null,
    val previousWork: String? = null,
    val handle: String? = null,
    val displayName: String? = null,
    val primaryRole: String? = null,
    val professionalName: String? = null,
    val skills: String? = null,
    val expertiseAreas: String? = null,
    val yearsExperience: Int? = null,
    val networkProfilePublic: Boolean? = null,
    val createdAt: String? = null,
) {
    val label: String
        get() = displayName ?: professionalName ?: name ?: handle?.let { "@$it" } ?: "Creator"
}

@Serializable
data class NetworkProfileContent(
    val id: String,
    val title: String = "Untitled",
    val type: String? = null,
    val posterUrl: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class NetworkChatsResponse(val conversations: List<NetworkConversation>? = null)

@Serializable
data class NetworkConversation(
    val id: String,
    val participants: List<NetworkAuthor>? = null,
    val lastMessage: NetworkChatMessage? = null,
)

@Serializable
data class NetworkChatThreadResponse(
    val conversationId: String? = null,
    val messages: List<NetworkChatMessage>? = null,
)

@Serializable
data class NetworkChatMessage(
    val id: String,
    val body: String? = null,
    val createdAt: String? = null,
    val sender: NetworkAuthor? = null,
)

@Serializable
data class CreateNetworkPostBody(
    val body: String? = null,
    val imageUrls: List<String>? = null,
    val contentId: String? = null,
)

@Serializable
data class ConnectBody(val message: String? = null)

@Serializable
data class ConnectStatusResponse(val status: String? = null)

// MARK: - Marketplace messages

@Serializable
data class MarketplaceMessage(
    val id: String,
    val body: String = "",
    val createdAt: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val requestId: String? = null,
    val locationBookingId: String? = null,
    val crewTeamRequestId: String? = null,
    val castingInquiryId: String? = null,
    val cateringBookingId: String? = null,
    val sender: MessageParty? = null,
    val receiver: MessageParty? = null,
    val request: EquipmentRequestContext? = null,
    val locationBooking: LocationBookingContext? = null,
    val crewTeamRequest: CrewRequestContext? = null,
    val castingInquiry: CastingInquiryContext? = null,
    val cateringBooking: CateringBookingContext? = null,
)

@Serializable
data class MessageParty(val id: String? = null, val name: String? = null, val role: String? = null)

@Serializable
data class EquipmentRequestContext(val id: String? = null, val equipment: EquipmentCompanyRef? = null)

@Serializable
data class EquipmentCompanyRef(val companyName: String? = null, val category: String? = null)

@Serializable
data class LocationBookingContext(val id: String? = null, val location: LocationNameRef? = null)

@Serializable
data class LocationNameRef(val name: String? = null, val type: String? = null)

@Serializable
data class CrewRequestContext(val id: String? = null, val crewTeam: CrewCompanyRef? = null)

@Serializable
data class CrewCompanyRef(val companyName: String? = null)

@Serializable
data class CastingInquiryContext(val id: String? = null, val agency: AgencyNameRef? = null)

@Serializable
data class AgencyNameRef(val agencyName: String? = null)

@Serializable
data class CateringBookingContext(val id: String? = null, val cateringCompany: CateringCompanyRef? = null)

@Serializable
data class CateringCompanyRef(val companyName: String? = null)

@Serializable
data class SendMarketplaceMessageBody(
    val body: String,
    val receiverId: String,
    val requestId: String? = null,
    val locationBookingId: String? = null,
    val crewTeamRequestId: String? = null,
    val castingInquiryId: String? = null,
    val cateringBookingId: String? = null,
)

// MARK: - Content / catalogue

@Serializable
data class CreatorContentItem(
    val id: String,
    val title: String = "Untitled",
    val description: String? = null,
    val type: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val videoUrl: String? = null,
    val trailerUrl: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val language: String? = null,
    val country: String? = null,
    val year: Int? = null,
    val duration: Int? = null,
    val episodes: Int? = null,
    val reviewStatus: String? = null,
    val reviewNote: String? = null,
    val reviewFeedback: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
    val published: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val linkedProjectId: String? = null,
    @SerialName("_count") val count: ContentCounts? = null,
    val ratings: List<ContentRatingScore>? = null,
    val linkedProject: LinkedProjectRef? = null,
    val seasons: List<ContentSeasonRef>? = null,
    val stream: ContentStreamInfo? = null,
) {
    val avgRating: Double?
        get() {
            val r = ratings ?: return null
            if (r.isEmpty()) return null
            return r.sumOf { it.score }.toDouble() / r.size
        }
}

@Serializable
data class ContentCounts(
    val watchSessions: Int? = null,
    val ratings: Int? = null,
    val comments: Int? = null,
    val seasons: Int? = null,
)

@Serializable
data class ContentRatingScore(val score: Int)

@Serializable
data class LinkedProjectRef(val id: String? = null, val title: String? = null)

@Serializable
data class ContentSeasonRef(
    val id: String,
    val seasonNumber: Int? = null,
    val title: String? = null,
    val published: Boolean? = null,
)

@Serializable
data class ContentStreamInfo(
    val video: StreamAssetStatus? = null,
    val trailer: StreamAssetStatus? = null,
)

@Serializable
data class StreamAssetStatus(val status: String? = null, val playbackUrl: String? = null)

@Serializable
data class CreateContentBody(
    val contentId: String? = null,
    val title: String,
    val type: String,
    val description: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val videoUrl: String? = null,
    val trailerUrl: String? = null,
    val scriptUrl: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val language: String? = null,
    val country: String? = null,
    val ageRating: String? = null,
    val year: Int? = null,
    val duration: Int? = null,
    val episodes: Int? = null,
    val linkedProjectId: String? = null,
    val reviewStatus: String? = null,
)

// MARK: - Upload

@Serializable
data class PresignRequest(
    val fileName: String,
    val size: Int,
    val contentType: String? = null,
)

@Serializable
data class PresignResponse(
    val uploadUrl: String,
    val key: String,
    val contentType: String = "application/octet-stream",
    val headers: PresignHeaders? = null,
)

@Serializable
data class PresignHeaders(
    @SerialName("Content-Type") val contentType: String? = null,
)

@Serializable
data class UploadCompleteRequest(
    val key: String,
    val contentType: String? = null,
    val fileName: String? = null,
)

@Serializable
data class UploadCompleteResponse(
    val ok: Boolean? = null,
    val storageRef: String? = null,
    val publicUrl: String? = null,
    val sourceUrl: String? = null,
    val streamPlaybackUrl: String? = null,
    val streamHlsUrl: String? = null,
) {
    val resolvedURL: String? get() = storageRef ?: publicUrl ?: sourceUrl
}

// MARK: - Revenue

@Serializable
data class RevenueAPIResponse(
    val revenue: Double? = null,
    val watchTime: Double? = null,
    val share: Double? = null,
    val totalViews: Int? = null,
    val streamCount: Int? = null,
    val perViewRand: Double? = null,
    val perStreamRand: Double? = null,
    val projectedRevenue: Double? = null,
    val walletAvailable: Double? = null,
    val walletTotalEarnings: Double? = null,
    val banking: RevenueBanking? = null,
    val payouts: List<RevenuePayout>? = null,
)

@Serializable
data class RevenueBanking(
    val bankName: String? = null,
    val accountNumberLast4: String? = null,
    val accountType: String? = null,
    val verified: Boolean? = null,
)

@Serializable
data class RevenuePayout(
    val amount: Double? = null,
    val status: String? = null,
    val period: String? = null,
    val createdAt: String? = null,
)

// MARK: - Competition

@Serializable
data class CompetitionStatsResponse(
    val period: CompetitionPeriod? = null,
    val rank: Int? = null,
    val voteCount: Int? = null,
)

@Serializable
data class CompetitionPeriod(
    val id: String? = null,
    val name: String? = null,
    val endDate: String? = null,
)

// MARK: - Project workspace / activity

@Serializable
data class ProductionWorkspaceResponse(
    val activityFeed: List<ProjectActivityItem>? = null,
    val tasks: List<ProjectTaskItem>? = null,
    val taskSummary: Map<String, Int>? = null,
)

@Serializable
data class ProjectActivityItem(
    val id: String,
    val type: String? = null,
    val message: String? = null,
    val metadata: String? = null,
    val createdAt: String? = null,
    val user: ActivityUser? = null,
)

@Serializable
data class ActivityUser(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
)

@Serializable
data class ProjectTaskItem(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val dueDate: String? = null,
    val department: String? = null,
)

// MARK: - Tool activity row (UI)

data class ToolActivityRow(
    val id: String,
    val title: String,
    val detail: String? = null,
    val actorName: String? = null,
    val timestamp: String? = null,
    val kind: String? = null,
    val icon: String,
)
