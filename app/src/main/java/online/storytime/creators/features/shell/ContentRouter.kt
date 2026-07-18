package online.storytime.creators.features.shell

import androidx.compose.runtime.Composable
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.AppDestination
import online.storytime.creators.core.model.ProjectPhase
import online.storytime.creators.features.account.AccountScreen
import online.storytime.creators.features.catalogue.CatalogueScreen
import online.storytime.creators.features.catalogue.RevenueScreen
import online.storytime.creators.features.catalogue.UploadScreen
import online.storytime.creators.features.commandcenter.CommandCenterScreen
import online.storytime.creators.features.marketplace.CastingPortalScreen
import online.storytime.creators.features.marketplace.CateringScreen
import online.storytime.creators.features.marketplace.CrewScreen
import online.storytime.creators.features.marketplace.EquipmentScreen
import online.storytime.creators.features.marketplace.LegalInboxScreen
import online.storytime.creators.features.marketplace.LocationsScreen
import online.storytime.creators.features.marketplace.MusicScreen
import online.storytime.creators.features.messages.MessagesScreen
import online.storytime.creators.features.network.NetworkScreen
import online.storytime.creators.features.originals.OriginalsScreen
import online.storytime.creators.features.pipeline.PhaseHubScreen
import online.storytime.creators.features.projects.ProjectToolDetailScreen
import online.storytime.creators.features.projects.ProjectsScreen

@Composable
fun ContentRouter() {
    val router = Session.router

    // Project-scoped tool detail takes over the content area.
    val tool = router.selectedTool
    if (tool != null && router.isShowingProjectTool) {
        ProjectToolDetailScreen(tool = tool, projectId = router.selectedProjectId)
        return
    }

    when (router.destination) {
        AppDestination.commandCenter -> CommandCenterScreen()
        AppDestination.projects -> ProjectsScreen()
        AppDestination.network -> NetworkScreen()
        AppDestination.messages -> MessagesScreen()
        AppDestination.account -> AccountScreen()
        AppDestination.catalogue -> CatalogueScreen()
        AppDestination.upload -> UploadScreen()
        AppDestination.revenue -> RevenueScreen()
        AppDestination.originals -> OriginalsScreen()
        AppDestination.preProduction -> PhaseHubScreen(ProjectPhase.preProduction)
        AppDestination.production -> PhaseHubScreen(ProjectPhase.production)
        AppDestination.postProduction -> PhaseHubScreen(ProjectPhase.postProduction)
        AppDestination.cast -> CastingPortalScreen()
        AppDestination.crew -> CrewScreen()
        AppDestination.locations -> LocationsScreen()
        AppDestination.equipment -> EquipmentScreen()
        AppDestination.catering -> CateringScreen()
        AppDestination.music -> MusicScreen()
        AppDestination.legalInbox -> LegalInboxScreen()
    }
}
