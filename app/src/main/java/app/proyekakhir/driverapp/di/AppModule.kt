package app.proyekakhir.driverapp.di

import app.proyekakhir.core.domain.usecase.MyInteractor
import app.proyekakhir.core.domain.usecase.MyUseCase
import app.proyekakhir.driverapp.ui.auth.AuthViewModel
import app.proyekakhir.driverapp.ui.home.ui.balance.BalanceViewModel
import app.proyekakhir.driverapp.ui.home.ui.home.HomeViewModel
import app.proyekakhir.driverapp.ui.home.ui.transaction.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {
    factory<MyUseCase> { MyInteractor(get(), androidContext()) }
}

val viewModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { BalanceViewModel(get()) }
}