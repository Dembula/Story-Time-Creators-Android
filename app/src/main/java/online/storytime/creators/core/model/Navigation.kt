package online.storytime.creators.core.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppDestination(val title: String, val icon: String) {
    commandCenter("Command Center", "square.grid.2x2.fill"),
    projects("My Projects", "folder.fill"),
    network("Network", "person.3.fill"),
    messages("Messages", "bubble.left.and.bubble.right.fill"),
    account("My Account", "person.crop.circle.fill"),
    catalogue("My Catalogue", "film.stack.fill"),
    upload("Catalogue Upload", "arrow.up.doc.fill"),
    revenue("Revenue", "chart.line.uptrend.xyaxis"),
    originals("Originals", "star.circle.fill"),
    preProduction("Pre-Production", "list.clipboard.fill"),
    production("Production", "video.fill"),
    postProduction("Post-Production", "slider.horizontal.3"),
    cast("Casting Portal", "theatermasks.fill"),
    crew("Crew", "wrench.and.screwdriver.fill"),
    locations("Locations", "mappin.and.ellipse"),
    equipment("Equipment", "camera.fill"),
    catering("Catering", "fork.knife"),
    music("Music & Scoring", "music.note.list"),
    legalInbox("Legal Inbox", "doc.text.fill");

    companion object {
        val operating = listOf(commandCenter, projects, network, messages, account)
        val monetization = listOf(catalogue, upload, revenue)
        val pipeline = listOf(preProduction, production, postProduction)
    }
}

enum class ProjectPhase(val raw: String, val title: String) {
    preProduction("PRE_PRODUCTION", "Pre-Production"),
    production("PRODUCTION", "Production"),
    postProduction("POST_PRODUCTION", "Post-Production");

    val destination: AppDestination
        get() = when (this) {
            preProduction -> AppDestination.preProduction
            production -> AppDestination.production
            postProduction -> AppDestination.postProduction
        }
}

enum class ProjectTool(
    val raw: String,
    val label: String,
    val phase: ProjectPhase,
    val apiPathSegment: String,
    val icon: String,
) {
    // Pre
    ideaDevelopment("idea-development", "Idea Development", ProjectPhase.preProduction, "ideas", "lightbulb.fill"),
    scriptWriting("script-writing", "Script Writing", ProjectPhase.preProduction, "script", "pencil.and.outline"),
    scriptReview("script-review", "Script Review", ProjectPhase.preProduction, "script-review", "doc.text.magnifyingglass"),
    scriptBreakdown("script-breakdown", "Script Breakdown", ProjectPhase.preProduction, "breakdown", "list.bullet.rectangle"),
    budgetBuilder("budget-builder", "Budget Builder", ProjectPhase.preProduction, "budget", "dollarsign.circle.fill"),
    productionScheduling("production-scheduling", "Production Scheduling", ProjectPhase.preProduction, "schedule", "calendar"),
    castingPortal("casting-portal", "Casting Portal", ProjectPhase.preProduction, "casting", "theatermasks.fill"),
    crewMarketplace("crew-marketplace", "Crew Marketplace", ProjectPhase.preProduction, "crew", "person.3.fill"),
    locationMarketplace("location-marketplace", "Location Marketplace", ProjectPhase.preProduction, "visual-assets", "mappin.and.ellipse"),
    visualPlanning("visual-planning", "Visual Planning", ProjectPhase.preProduction, "visual-assets", "photo.on.rectangle.angled"),
    legalContracts("legal-contracts", "Legal & Contracts", ProjectPhase.preProduction, "contracts", "doc.text.fill"),
    fundingHub("funding-hub", "Funding Hub", ProjectPhase.preProduction, "funding", "banknote.fill"),
    tableReads("table-reads", "Table Reads", ProjectPhase.preProduction, "table-reads", "book.fill"),
    productionWorkspace("production-workspace", "Production Workspace", ProjectPhase.preProduction, "production-workspace", "folder.fill"),
    equipmentPlanning("equipment-planning", "Equipment Planning", ProjectPhase.preProduction, "equipment-plan", "camera.fill"),
    riskInsurance("risk-insurance", "Risk & Insurance", ProjectPhase.preProduction, "risk", "shield.fill"),
    productionReadiness("production-readiness", "Production Readiness", ProjectPhase.preProduction, "readiness", "checkmark.seal.fill"),

    // Production
    controlCenter("control-center", "Production Control Center", ProjectPhase.production, "production-control-center", "slider.horizontal.3"),
    callSheetGenerator("call-sheet-generator", "Call Sheet Generator", ProjectPhase.production, "call-sheets", "doc.richtext.fill"),
    onSetTasks("on-set-tasks", "On-Set Tasks", ProjectPhase.production, "tasks", "checklist"),
    equipmentTracking("equipment-tracking", "Equipment Tracking", ProjectPhase.production, "equipment-plan", "shippingbox.fill"),
    shootProgress("shoot-progress", "Shoot Progress", ProjectPhase.production, "shoot-progress", "chart.bar.fill"),
    continuityManager("continuity-manager", "Continuity Manager", ProjectPhase.production, "continuity", "film.fill"),
    dailiesReview("dailies-review", "Dailies Review", ProjectPhase.production, "dailies", "play.rectangle.fill"),
    expenseTracker("expense-tracker", "Expense Tracker", ProjectPhase.production, "expenses", "creditcard.fill"),
    incidentReporting("incident-reporting", "Incident Reporting", ProjectPhase.production, "incidents", "exclamationmark.triangle.fill"),
    onSetCatering("on-set-catering", "On-Set Catering", ProjectPhase.production, "vendors", "fork.knife"),
    wrap("wrap", "Production Wrap", ProjectPhase.production, "production-wrap", "flag.checkered"),

    // Post
    footageIngestion("footage-ingestion", "Footage Ingestion", ProjectPhase.postProduction, "footage", "arrow.down.doc.fill"),
    editingStudio("editing-studio", "Editing Studio", ProjectPhase.postProduction, "final-delivery", "scissors"),
    soundDesign("sound-design", "Sound Design", ProjectPhase.postProduction, "final-delivery", "waveform"),
    musicScoring("music-scoring", "Music & Scoring", ProjectPhase.postProduction, "music-selection", "music.note.list"),
    visualEffects("visual-effects", "Visual Effects", ProjectPhase.postProduction, "final-delivery", "sparkles"),
    colorGrading("color-grading", "Color Grading", ProjectPhase.postProduction, "final-delivery", "paintpalette.fill"),
    finalSoundMix("final-sound-mix", "Final Sound Mix", ProjectPhase.postProduction, "final-delivery", "speaker.wave.3.fill"),
    finalCutApproval("final-cut-approval", "Final Cut Approval", ProjectPhase.postProduction, "final-delivery", "checkmark.circle.fill"),
    filmPackaging("film-packaging", "Film Packaging", ProjectPhase.postProduction, "final-delivery", "shippingbox.fill"),
    distribution("distribution", "Distribution", ProjectPhase.postProduction, "distribution", "globe");

    val isMarketplaceStyle: Boolean
        get() = this in setOf(
            castingPortal, crewMarketplace, locationMarketplace,
            equipmentPlanning, onSetCatering, musicScoring, distribution,
        )

    companion object {
        fun tools(phase: ProjectPhase): List<ProjectTool> = entries.filter { it.phase == phase }

        fun hubTools(phase: ProjectPhase): List<ProjectTool> = when (phase) {
            ProjectPhase.postProduction -> listOf(musicScoring, distribution)
            else -> tools(phase)
        }

        fun fromRaw(raw: String): ProjectTool? = entries.firstOrNull { it.raw == raw }
    }
}

object ProjectPhaseResolver {
    fun resolve(project: CreatorProject): ProjectPhase =
        when ((project.phase ?: project.status ?: "").uppercase()) {
            "PRODUCTION" -> ProjectPhase.production
            "POST_PRODUCTION", "POST-PRODUCTION" -> ProjectPhase.postProduction
            else -> ProjectPhase.preProduction
        }
}

/** State-driven router mirroring the iOS AppRouter (Compose snapshot-backed). */
class AppRouter {
    var destination by mutableStateOf(AppDestination.commandCenter)
    var isSideMenuOpen by mutableStateOf(false)
    var selectedProjectId by mutableStateOf<String?>(null)
    var selectedTool by mutableStateOf<ProjectTool?>(null)
    var toolReturnDestination by mutableStateOf<AppDestination?>(null)

    val isShowingProjectTool: Boolean
        get() {
            val tool = selectedTool ?: return false
            return !tool.isMarketplaceStyle
        }

    fun open(dest: AppDestination) {
        destination = dest
        selectedTool = null
        toolReturnDestination = null
        if (dest == AppDestination.projects) {
            selectedProjectId = null
        }
        isSideMenuOpen = false
    }

    fun toggleMenu() { isSideMenuOpen = !isSideMenuOpen }
    fun closeMenu() { isSideMenuOpen = false }

    fun openProject(id: String) {
        selectedProjectId = id
        destination = AppDestination.projects
        selectedTool = null
        toolReturnDestination = null
        closeMenu()
    }

    fun openTool(tool: ProjectTool, projectId: String?) {
        if (!tool.isMarketplaceStyle) {
            if (projectId.isNullOrEmpty()) return
        }
        toolReturnDestination = destination
        selectedTool = tool
        selectedProjectId = projectId ?: selectedProjectId

        when (tool) {
            ProjectTool.castingPortal -> destination = AppDestination.cast
            ProjectTool.crewMarketplace -> destination = AppDestination.crew
            ProjectTool.locationMarketplace -> destination = AppDestination.locations
            ProjectTool.equipmentPlanning -> destination = AppDestination.equipment
            ProjectTool.onSetCatering -> destination = AppDestination.catering
            ProjectTool.musicScoring -> destination = AppDestination.music
            ProjectTool.distribution -> destination = AppDestination.upload
            else -> { /* project-scoped tool detail rendered in shell */ }
        }
        closeMenu()
    }

    fun leaveToolDetail() {
        val returnTo = toolReturnDestination
        selectedTool = null
        toolReturnDestination = null
        if (returnTo != null) destination = returnTo
    }
}
