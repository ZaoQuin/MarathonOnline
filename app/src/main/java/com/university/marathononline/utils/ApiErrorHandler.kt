import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.repository.AuthRepository
import com.university.marathononline.ui.view.activity.SplashRedirectActivity
import com.university.marathononline.utils.startNewActivity

fun <T> Fragment.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> {
            logError("Unauthorized access - 401", failure)
        }
        failure.errorCode == 403 -> {
            showToast("You don't have permission to access this resource.")
            logError("Forbidden - 403", failure)
        }
        failure.errorCode == 404 -> {
            showToast("Resource not found. Please try again.")
            logError("Not Found - 404", failure)
        }
        failure.errorCode == 408 -> {
            showToast("Request timed out. Please try again.")
            logError("Request Timeout - 408", failure)
        }
        failure.errorCode == 500 -> {
            showToast("Something went wrong on the server, please try again later.")
            logError("Internal Server Error - 500", failure)
        }
        failure.isNetworkError -> {
            showToast("Please check your internet connection.")
            logError("Network Error", failure)
        }
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                showToast(errorMessage)
                logError("API Error: $errorMessage", failure)
            } else {
                showToast("An unknown error occurred.")
                logError("Unknown Error", failure)
            }
        }
    }
    retry?.let {
        showRetryDialog(it)
    }
}

private fun <T> Fragment.logError(message: String, failure: Resource.Failure<T>) {
    Log.e("ApiError", "$message. Error Code: ${failure.errorCode}, Error Body: ${failure.errorBody}")
}


private fun Fragment.showRetryDialog(retry: () -> Unit) {
    AlertDialog.Builder(requireContext()).apply {
        setTitle("Retry")
        setMessage("Do you want to retry?")
        setPositiveButton("Yes") { _, _ -> retry() }
        setNegativeButton("No", null)
        create()
        show()
    }
}

fun <T> AppCompatActivity.handleApiError(
    failure: Resource.Failure<T>,
    repository: AuthRepository? = null,
    retry: (() -> Unit)? = null
) {
    when {
        failure.errorCode == 401 -> {
            logError("Unauthorized access - 401", failure)
        }
        failure.errorCode == 403 -> {
            showToast("You don't have permission to access this resource.")
            logError("Forbidden - 403", failure)
        }
        failure.errorCode == 404 -> {
            showToast("Resource not found. Please try again.")
            logError("Not Found - 404", failure)
        }
        failure.errorCode == 408 -> {
            showToast("Request timed out. Please try again.")
            logError("Request Timeout - 408", failure)
        }
        failure.errorCode == 500 -> {
            showToast("Something went wrong on the server, please try again later.")
            logError("Internal Server Error - 500", failure)
        }
        failure.isNetworkError -> {
            showToast("Please check your internet connection.")
            logError("Network Error", failure)
        }
        else -> {
            val errorMessage = failure.errorBody?.string().orEmpty()
            if (errorMessage.isNotBlank()) {
                showToast(errorMessage)
                logError("API Error: $errorMessage", failure)
            } else {
                showToast("An unknown error occurred.")
                logError("Unknown Error", failure)
            }
        }
    }
    retry?.let {
        showRetryDialog(it)
    }
}

private fun <T> AppCompatActivity.logError(message: String, failure: Resource.Failure<T>) {
    Log.e("ApiError", "$message. Error Code: ${failure.errorCode}, Error Body: ${failure.errorBody}")
}

private fun AppCompatActivity.showRetryDialog(retry: () -> Unit) {
    AlertDialog.Builder(this).apply {
        setTitle("Retry")
        setMessage("Do you want to retry?")
        setPositiveButton("Yes") { _, _ -> retry() }
        setNegativeButton("No", null)
        create()
        show()
    }
}

private fun Fragment.navigateToLogin() {
    startNewActivity(SplashRedirectActivity::class.java, true)
}

private fun AppCompatActivity.navigateToLogin() {
    startNewActivity(SplashRedirectActivity::class.java, true)
}

private fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

private fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
