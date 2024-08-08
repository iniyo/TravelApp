package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    // ViewBinding 객체를 저장하는 변수
    protected var _binding: T? = null
    val binding get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding 객체를 생성하여 _binding 변수에 할당
        _binding = createBinding(inflater, container)
        initCreate()
        return _binding?.root
    }

    @Suppress("UNCHECKED_CAST")
    private fun createBinding(inflater: LayoutInflater, container: ViewGroup?): T {
        // 현재 클래스의 수퍼 클래스에 대한 타입 정보 가져옴
        val superclass: Type? = javaClass.genericSuperclass
        if (superclass !is ParameterizedType) {
            throw IllegalStateException("Superclass must be parameterized")
        }
        // 수퍼 클래스의 제네릭 타입 인수 중 첫 번째 인수를 가져와 ViewBinding으로 캐스트
        val aClass = (superclass.actualTypeArguments[0] as Class<T>)
        return if (ViewDataBinding::class.java.isAssignableFrom(aClass)) {
            // ViewDataBinding의 서브 클래스인 경우 DataBindingUtil을 사용하여 바인딩 초기화
            val method = aClass.getMethod("inflate", LayoutInflater::class.java)
            method.invoke(null, inflater) as T
        } else {
            // ViewBinding의 서브 클래스인 경우 직접 바인딩 초기화
            val method = aClass.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            method.invoke(null, inflater, container, false) as T
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind {
            initView()
            initAdapter()
            initViewModel()
            initListener()
            afterViewCreated()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 하위 클래스에서 구현할 수 있는 초기화 메서드들
    protected open fun initCreate() {}
    protected open fun initView() {}
    protected open fun initViewModel() {}
    protected open fun initListener() {}
    protected open fun afterViewCreated() {}
    protected open fun initAdapter() {}
    open fun handleBackPressed(): Boolean {
        return true
    }
    // ViewBinding 객체에 대해 블록 코드를 실행하는 메서드
    protected inline fun bind(crossinline block: T.() -> Unit) {
        _binding?.apply(block)
    }

    // Lifecycle 상태가 STARTED일 때 코루틴 블록을 실행하는 메서드
    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }

}

