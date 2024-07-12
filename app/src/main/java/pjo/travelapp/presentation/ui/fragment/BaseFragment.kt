package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
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

abstract class BaseFragment<T : ViewBinding>(@LayoutRes private val layoutId: Int) : Fragment() {

    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container)
        initCreate()
        return binding.root
    }

    @Suppress("UNCHECKED_CAST") // 안전하지 않은 캐스트 타입 캐스트 시 경고 무시
    private fun createBinding(inflater: LayoutInflater, container: ViewGroup?): T {
        val superclass: Type? = javaClass.genericSuperclass // 현재 클래스의 수퍼 클래스에 대한 타입 정보 가져옴. -> 제네릭 타입 포함 수퍼 클래스 반환
        if (superclass !is ParameterizedType) {
            throw IllegalStateException("Superclass must be parameterized")
        }
        val aClass = superclass.actualTypeArguments[0] as Class<T> // ParameterizedType -> 제네릭 타입정보 포함하는 타입, actualTypeArguments[0] 수퍼 클래스의 제네릭 타입 인수 중 첫 번째, 이후 T타입으로 캐스트
        // view data binding 혹은 서브 클래스인지 확인
        return if (ViewDataBinding::class.java.isAssignableFrom(aClass)) {
            // 맞으면 data binding으로 binding
            DataBindingUtil.inflate(inflater, layoutId, container, false)
        } else {
            // 틀리면 view binding으로 binding
            val method = aClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java) // invoke로 받을 메서드 형식 지정
            method.invoke(null, inflater, container, false) as T // method.invoke로 inflate 메서드 호출 -> 타입에 맞는걸로 조정됨.
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        initListener()
        afterViewCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected open fun initCreate() {}
    protected open fun initView() {}
    protected open fun initViewModel() {}
    protected open fun initListener() {}
    protected open fun afterViewCreated() {}
    protected inline fun bind(block: T.() -> Unit) {
        binding.apply(block)
    }
    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }
}
