package online.storytime.creators.core.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps the iOS SF Symbol names used throughout the app to Material icons so the
 * Android UI keeps the same visual intent.
 */
fun stIcon(name: String): ImageVector = when (name) {
    "square.grid.2x2.fill" -> Icons.Filled.GridView
    "folder.fill", "folder" -> Icons.Filled.Folder
    "folder.badge.plus" -> Icons.Filled.CreateNewFolder
    "person.3.fill", "person.3" -> Icons.Filled.Groups
    "person.2.fill" -> Icons.Filled.People
    "bubble.left.and.bubble.right.fill" -> Icons.Filled.Forum
    "person.crop.circle.fill" -> Icons.Filled.AccountCircle
    "person.crop.circle.badge.plus", "person.badge.plus" -> Icons.Filled.PersonAdd
    "film.stack.fill", "film.stack", "film.fill", "film" -> Icons.Filled.Movie
    "arrow.up.doc.fill" -> Icons.Filled.UploadFile
    "chart.line.uptrend.xyaxis" -> Icons.Filled.TrendingUp
    "star.circle.fill", "star.circle" -> Icons.Filled.Stars
    "list.clipboard.fill" -> Icons.Filled.Assignment
    "video.fill" -> Icons.Filled.Videocam
    "slider.horizontal.3" -> Icons.Filled.Tune
    "theatermasks.fill", "theatermasks" -> Icons.Filled.TheaterComedy
    "wrench.and.screwdriver.fill" -> Icons.Filled.Build
    "mappin.and.ellipse", "mappin" -> Icons.Filled.Place
    "camera.fill" -> Icons.Filled.PhotoCamera
    "fork.knife" -> Icons.Filled.Restaurant
    "music.note.list", "music.note", "music.note.tv" -> Icons.Filled.LibraryMusic
    "doc.text.fill", "doc.text" -> Icons.Filled.Description
    "lightbulb.fill" -> Icons.Filled.Lightbulb
    "pencil.and.outline" -> Icons.Filled.Edit
    "doc.text.magnifyingglass" -> Icons.Filled.ManageSearch
    "list.bullet.rectangle" -> Icons.Filled.ViewList
    "dollarsign.circle.fill" -> Icons.Filled.MonetizationOn
    "calendar" -> Icons.Filled.CalendarMonth
    "photo.on.rectangle.angled", "photo" -> Icons.Filled.Image
    "rectangle.on.rectangle" -> Icons.Filled.Layers
    "banknote.fill" -> Icons.Filled.Payments
    "book.fill" -> Icons.Filled.MenuBook
    "shield.fill" -> Icons.Filled.Shield
    "checkmark.seal.fill" -> Icons.Filled.VerifiedUser
    "doc.richtext.fill", "doc.richtext" -> Icons.Filled.Article
    "checklist" -> Icons.Filled.Checklist
    "shippingbox.fill" -> Icons.Filled.Inventory2
    "chart.bar.fill", "chart.bar.doc.horizontal" -> Icons.Filled.BarChart
    "play.rectangle.fill", "play.rectangle" -> Icons.Filled.SmartDisplay
    "creditcard.fill" -> Icons.Filled.CreditCard
    "exclamationmark.triangle.fill" -> Icons.Filled.Warning
    "flag.checkered" -> Icons.Filled.Flag
    "arrow.down.doc.fill" -> Icons.Filled.Download
    "scissors" -> Icons.Filled.ContentCut
    "waveform" -> Icons.Filled.GraphicEq
    "sparkles", "sparkles.rectangle.stack" -> Icons.Filled.AutoAwesome
    "paintpalette.fill" -> Icons.Filled.Palette
    "speaker.wave.3.fill" -> Icons.Filled.VolumeUp
    "checkmark.circle.fill" -> Icons.Filled.CheckCircle
    "globe" -> Icons.Filled.Public
    "eye.fill", "eye" -> Icons.Filled.Visibility
    "heart.fill" -> Icons.Filled.Favorite
    "heart" -> Icons.Filled.FavoriteBorder
    "timer" -> Icons.Filled.Timer
    "bubble.left.fill", "bubble.left" -> Icons.Filled.ChatBubble
    "bubble.right", "text.bubble.fill" -> Icons.Filled.Comment
    "star.fill" -> Icons.Filled.Star
    "bookmark.fill" -> Icons.Filled.Bookmark
    "chart.pie.fill" -> Icons.Filled.PieChart
    "bolt.fill" -> Icons.Filled.Bolt
    "clock.fill", "clock" -> Icons.Filled.Schedule
    "person.fill" -> Icons.Filled.Person
    "arrow.clockwise" -> Icons.Filled.Refresh
    "line.3.horizontal" -> Icons.Filled.Menu
    "xmark" -> Icons.Filled.Close
    "xmark.circle.fill" -> Icons.Filled.Cancel
    "rectangle.portrait.and.arrow.right" -> Icons.Filled.Logout
    "plus.circle.fill" -> Icons.Filled.AddCircle
    "plus.circle" -> Icons.Filled.AddCircleOutline
    "chevron.right" -> Icons.Filled.ChevronRight
    "chevron.left" -> Icons.Filled.ChevronLeft
    "chevron.up" -> Icons.Filled.ExpandLess
    "chevron.down" -> Icons.Filled.ExpandMore
    "chevron.up.chevron.down" -> Icons.Filled.UnfoldMore
    "info.circle.fill" -> Icons.Filled.Info
    "magnifyingglass" -> Icons.Filled.Search
    "building.2" -> Icons.Filled.Business
    "building.columns.fill" -> Icons.Filled.AccountBalance
    "trash" -> Icons.Filled.Delete
    "ellipsis.circle" -> Icons.Filled.MoreVert
    "paperplane.fill" -> Icons.Filled.Send
    "arrow.up.circle.fill" -> Icons.Filled.ArrowCircleUp
    "safari.fill" -> Icons.Filled.OpenInBrowser
    "trophy.fill" -> Icons.Filled.EmojiEvents
    "hand.thumbsup.fill" -> Icons.Filled.ThumbUp
    "wallet.pass.fill" -> Icons.Filled.AccountBalanceWallet
    "percent" -> Icons.Filled.Percent
    "play.circle.fill" -> Icons.Filled.PlayCircle
    "tv" -> Icons.Filled.Tv
    "mic.fill" -> Icons.Filled.Mic
    "face.smiling" -> Icons.Filled.SentimentSatisfied
    "sportscourt.fill" -> Icons.Filled.Stadium
    "newspaper.fill" -> Icons.Filled.Newspaper
    "arrow.up.forward.app.fill" -> Icons.Filled.Launch
    "tray" -> Icons.Filled.Inbox
    "bubble" -> Icons.Filled.ChatBubbleOutline
    "person.crop.circle" -> Icons.Filled.AccountCircle
    else -> Icons.Filled.Widgets
}
