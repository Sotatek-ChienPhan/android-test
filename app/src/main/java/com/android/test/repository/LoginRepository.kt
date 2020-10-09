package setel.com.android.repository

import com.android.test.base.BaseRepository
import com.android.test.model.User
import kotlinx.coroutines.flow.flow
import com.android.test.network.Resource
import com.android.test.network.eitherNetwork
import com.android.test.network.service.LoginService
import com.android.test.network.toResource
import com.android.test.repository.UserDao
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository  @Inject constructor(private val loginService: LoginService, private val userDao: UserDao) : BaseRepository() {

    fun login(request: JsonObject) = flow {
        emit(Resource.Loading())
        emit(eitherNetwork { loginService.login(request) }.toResource())
    }

    fun saveUser(user: User?) {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.insert(user)
        }
    }
}