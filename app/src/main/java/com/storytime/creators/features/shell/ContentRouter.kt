package com.storytime.creators.features.shell

import androidx.compose.runtime.Composable
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.AppDestination
import com.storytime.creators.core.model.ProjectPhase
import com.storytime.creators.features.account.AccountScreen
import com.storytime.creators.features.catalogue.CatalogueScreen
import com.storytime.creators.features.catalogue.RevenueScreen
import com.storytime.creators.features.catalogue.UploadScreen
import com.storytime.creators.features.commandcenter.CommandCenterScreen
import com.storytime.creators.features.marketplace.CastingPortalScreen
import com.storytime.creators.features.marketplace.CateringScreen
import com.storytime.creators.features.marketplace.CrewScreen
import com.storytime.creators.features.marketplace.EquipmentScreen
import com.storytime.creators.features.marketplace.LegalInboxScreen
import com.storytime.creators.features.marketplace.LocationsScreen
import com.storytime.creators.features.marketplace.MusicScreen
import com.storytime.creators.features.messages.MessagesScreen
import com.storytime.creators.features.network.NetworkScreen
import com.storytime.creators.features.originals.OriginalsScreen
import com.storytime.creators.features.pipeline.PhaseHubScreen
import com.storytime.creators.features.projects.ProjectToolDetailScreen
import com.storytime.creators.features.projects.ProjectsScreen

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
