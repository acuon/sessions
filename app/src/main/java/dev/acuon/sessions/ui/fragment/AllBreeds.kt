package dev.acuon.sessions.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.acuon.sessions.ApplicationClass
import dev.acuon.sessions.R
import dev.acuon.sessions.databinding.FragmentAllBreedsBinding
import dev.acuon.sessions.ui.adapter.DogAdapter
import dev.acuon.sessions.ui.listener.ClickListener
import dev.acuon.sessions.ui.listener.MainActivityInterface
import dev.acuon.sessions.utils.Extensions.showToast
import dev.acuon.sessions.utils.NetworkUtils
import dev.acuon.sessions.viewmodel.MainViewModel
import dev.acuon.sessions.viewmodel.MainViewModelFactory

class AllBreeds : Fragment(), ClickListener {
    private lateinit var binding: FragmentAllBreedsBinding
    private lateinit var mainActivityInterface: MainActivityInterface
    private lateinit var viewModel: MainViewModel
    private lateinit var breedName: ArrayList<String>
    private lateinit var connectionLiveData: NetworkUtils
    private lateinit var dogAdapter: DogAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            initNavigationInterface(it)
        }
        binding = FragmentAllBreedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initNavigationInterface(context: FragmentActivity) {
        mainActivityInterface = context as MainActivityInterface
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityInterface.setFragmentName("Dog Breeds")
        connectionLiveData = NetworkUtils(activity?.applicationContext!!)
        connectionLiveData.observe(requireActivity()) { connection ->
            binding.apply {
                if (connection!!.isConnected) {
                    mainActivityInterface.progressBar(true)
                    initialize()
                } else {
                    requireContext().showToast("Internet not connected")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivityInterface.setFragmentName("Dog Breeds")
    }

    private fun initialize() {
        setAdapter()
        initViewModel()
        getData()
    }

    private fun setAdapter() {
        breedName = ArrayList()
        dogAdapter = DogAdapter(this)
        dogAdapter.differ.submitList(breedName)
        binding.rcv.apply {
            adapter = dogAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initViewModel() {
        val repository = (requireActivity().application as ApplicationClass).repository
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, "affenpinscher")
        )[MainViewModel::class.java]
    }

    private fun getData() {
        breedName.clear()
        viewModel.breeds.observe(viewLifecycleOwner, Observer {
            mainActivityInterface.progressBar(false)
            breedName.addAll(it)
            Log.d("response-${breedName.size}", breedName.toString())
            mainActivityInterface.progressBar(false)
            dogAdapter.notifyDataSetChanged()
        })
    }

    override fun onClick(position: Int) {
        requireContext().showToast(breedName[position]+" clicked")
        val breedImages = BreedImages()
        breedImages.arguments = Bundle().apply {
            putString("name", breedName[position].toString())
            putString("previous", AllBreeds::class.java.name)
        }
        mainActivityInterface.openFragment(breedImages)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityInterface.setFragmentName("Random Breeds")
    }
}