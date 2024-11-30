import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.university.marathononline.base.BaseActivity
import com.university.marathononline.base.BaseFragment
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.data.request.RefreshTokenRequest
import kotlinx.coroutines.launch

fun <T> Fragment.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> handleUnauthorizedError(repository, retry)
        failure.errorCode == 403 -> showToast("You don't have permission to access this resource.")
        failure.errorCode == 500 -> showToast("Something went wrong on the server, please try again later.")
        failure.isNetworkError -> showToast("Please check your internet connection")
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                showToast(errorMessage)
            } else {
                showToast("An unknown error occurred.")
            }
        }
    }
}

fun <T> AppCompatActivity.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> handleUnauthorizedError(repository, retry)
        failure.errorCode == 403 -> showToast("You don't have permission to access this resource.")
        failure.errorCode == 500 -> showToast("Something went wrong on the server, please try again later.")
        failure.isNetworkError -> showToast("Please check your internet connection")
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                showToast(errorMessage)
            } else {
                showToast("An unknown error occurred.")
            }
        }
    }
}

private fun Fragment.handleUnauthorizedError(
    repository: AuthRepository?,
    retry: (() -> Unit)?
) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                showToast("Token refreshed successfully")

                retry?.invoke()
            } else {
                logout()
                showToast("Failed to refresh token")
            }
        } else {
            logout()
            showToast("Unable to get user data")
        }
    }
}

private fun AppCompatActivity.handleUnauthorizedError(
    repository: AuthRepository?,
    retry: (() -> Unit)?
) {
    lifecycleScope.launch {
        val userResult = repository?.getUser()
        if (userResult is Resource.Success) {
            val refreshResult = repository.refreshAccessToken(
                RefreshTokenRequest(userResult.value.refreshToken)
            )
            if (refreshResult is Resource.Success) {
                val newToken = refreshResult.value.token
                repository.saveAuthToken(newToken)
                showToast("Token refreshed successfully")

                retry?.invoke()
            } else {
                logout()
                showToast("Failed to refresh token")
            }
        } else {
            logout()
            showToast("Unable to get user data")
        }
    }
}

private fun Fragment.logout() {
    (this as BaseFragment<*, *>).logout()
}

private fun AppCompatActivity.logout() {
    (this as BaseActivity<*, *>).logout()
}

private fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

private fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
