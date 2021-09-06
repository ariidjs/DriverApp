package app.proyekakhir.driverapp.di

import app.proyekakhir.driverapp.ui.home.ui.home.HomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    viewModel { HomeViewModel(get()) }
}