package pl.devpotop.testowear

import android.graphics.Bitmap
import android.graphics.Color
import androidx.fragment.app.Fragment

import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position

import android.util.Log

import android.os.*
import android.view.*
import android.widget.Toast
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.ar.core.ArCoreApk


import com.google.ar.core.Config
import io.github.sceneview.ar.arcore.instantPlacementEnabled
import io.github.sceneview.ar.arcore.planeFindingEnabled
import io.github.sceneview.ar.node.infos.TapArPlaneInfoNode
import pl.devpotop.testowear.databinding.FragmentArBinding


class ArFragment : Fragment() {

    lateinit var modelNode: ArModelNode


    private var _binding: FragmentArBinding? = null
    private val binding get() = _binding!!

    fun actionButtonClicked() {

        if (!modelNode.isAnchored) {

            binding.objectManipulator.text = getString(R.string.remove_object)
            placeObject()
        } else {
            removeObejct()
            binding.objectManipulator.text = getString(R.string.place_object)
        }

    }
    fun removeObejct(){
        binding.sceneView.removeChild(modelNode)
        binding.sceneView.planeRenderer.isVisible = true
        modelNode.parent=null
        modelNode.anchor?.detach()
        modelNode.anchor=null


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
        return view

    }

    fun placeObject(){
        val arFrame= binding.sceneView.arSession?.currentFrame
        val hits=arFrame?.hitTest(approximateDistanceMeters = 1.0f, plane = true, instantPlacement = false)
        val anchor=   hits?.createAnchor()
        binding.sceneView.addChild(modelNode)
        modelNode.anchor=anchor
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkArCompability()
        binding.sceneView.onArSessionCreated = { arSession: ArSession ->

        }

        binding.sceneView.onArSessionFailed = { exception: Exception ->


        }

        modelNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL).apply {
            loadModelAsync(
                context = requireContext(),
                glbFileLocation = "models/model.glb",
                autoAnimate = false,
                autoScale = false,
                centerOrigin = Position(y=-1.0f)
            )
        }
        modelNode.scaleModel(units = 0.7f)

        binding.sceneView. onTouchAr = { hitResult, _ ->
                if(!modelNode.isAnchored){
                    val anchor=   hitResult.createAnchor()
                    binding.sceneView.addChild(modelNode)
                    modelNode.anchor=anchor
                    binding.objectManipulator.text = getString(R.string.remove_object)
                }

        }

        binding.averageColor.setOnClickListener(View.OnClickListener {
            averageColorButtonClicked()
        })

        binding.objectManipulator.setOnClickListener(View.OnClickListener {
            actionButtonClicked()
        })


        }


    fun configureSession(arSession: ArSession) {
        arSession.configure(
            arSession.config.apply {
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                // Depth API is used if it is configured in Hello AR's settings.
                depthMode =
                    if (arSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        Config.DepthMode.AUTOMATIC
                    } else {
                        Config.DepthMode.DISABLED
                    }
                planeFindingEnabled=true
                planeFindingMode =Config.PlaneFindingMode.HORIZONTAL
                instantPlacementEnabled=true
                instantPlacementMode=Config.InstantPlacementMode.LOCAL_Y_UP


            }
        )
    }

    fun averageColorButtonClicked() {

        usePixelCopy(binding.sceneView) { bitmap: Bitmap? ->
            val averageColorInt = bitmap?.let { averageColorCount(it) }
            averageColorInt?.let { binding.averageColor.setBackgroundColor(it) }

        }

    }

    override fun onResume() {
        super.onResume()
        binding.sceneView.arSession?.let { configureSession(it) }

    }

    fun usePixelCopy(videoView: SurfaceView, callback: (Bitmap?) -> Unit) {
        val bitmap: Bitmap = Bitmap.createBitmap(
            videoView.width,
            videoView.height,
            Bitmap.Config.ARGB_8888
        );
        try {
            // Create a handler thread to offload the processing of the image.
            val handlerThread = HandlerThread("PixelCopier");
            handlerThread.start();
            PixelCopy.request(
                videoView, bitmap,
                PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                    handlerThread.quitSafely();
                },
                Handler(handlerThread.looper)
            )
        } catch (e: IllegalArgumentException) {
            callback(null)
            e.printStackTrace()
        }
    }


    fun averageColorCount(bitmap: Bitmap): Int {
        var redBucket: Long = 0
        var greenBucket: Long = 0
        var blueBucket: Long = 0
        var pixelCount: Long = 0

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val c: Int = bitmap.getPixel(x, y)
                pixelCount++

                redBucket+=Color.red(c)
                greenBucket+=Color.green(c)
                blueBucket+=Color.blue(c)
            }
        }

        return Color.rgb(
            (redBucket / pixelCount).toInt(), (
                    greenBucket / pixelCount).toInt(), (
                    blueBucket / pixelCount).toInt()
        )
    }

    fun checkArCompability(){

        if ( binding.sceneView.arCore.isInstalled(requireContext())) {
            binding.objectManipulator.visibility = View.VISIBLE
            binding.objectManipulator.isEnabled = true
        } else { // The device is unsupported
            binding.objectManipulator.visibility = View.INVISIBLE
            binding.objectManipulator.isEnabled = false
            Handler().postDelayed({
                checkArCompability()
            }, 200)
        }
        }
    }


