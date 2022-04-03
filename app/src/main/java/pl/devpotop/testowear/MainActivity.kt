package pl.devpotop.testowear

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.ar.sceneform.rendering.Texture
import io.github.sceneview.ar.scene.PlaneRenderer
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.Config
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.ar.node.infos.SearchPlaneInfoNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import java.nio.ByteOrder


class MainActivity : AppCompatActivity() {
    lateinit var modelNode: ArModelNode;
    lateinit var arSceneView: ArSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.commit {
            add(R.id.fragmentContainerView, pl.devpotop.testowear.ArFragment::class.java, Bundle())
        }
    }
}



/*

       modelNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL).apply {
          loadModelAsync(
              context = this@MainActivity,
                glbFileLocation = "models/model.glb",
          autoAnimate = true,
          autoScale = false,
          // Place the model origin at the bottom center
          centerOrigin = Position(y = -1.0f)
          )
      }
     //   modelNode.placementMode.depthEnabled
      //  modelNode.placementPosition

        sceneView.cameraDistance=1000f;

        place_object.setOnClickListener(View.OnClickListener {

            placeObjectButtonClicked()})
    }

   /* fun prepareConfig(){

        val config = Config(arSession)
        config.depthMode = Config.DepthMode.AUTOMATIC



    }
     override fun  onResume(){
        super.onResume()
         val config = Config(arSession)
     }
*/
    fun checkArCompability(){
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            Handler().postDelayed({
                checkArCompability()
            }, 200)
        }
        if (availability.isSupported) {
            place_object.visibility = View.VISIBLE
            place_object.isEnabled = true
        } else { // The device is unsupported
            place_object.visibility = View.INVISIBLE
            place_object.isEnabled = false
        }
    }

    fun placeObjectButtonClicked() {
        if(!sceneView.children.contains(modelNode)){
            sceneView.addChild(modelNode)
            modelNode.anchor();
            place_object.text = getString(R.string.remove_object)
            sceneView.planeRenderer.isVisible = true

        }else{
            place_object.text = getString(R.string.place_object)
            modelNode.anchor=null
            sceneView.planeRenderer.isVisible = false
        }
     /*
        if (!modelNode.isAnchored && modelNode.anchor()) {
            place_object.text = getString(R.string.remove_object)
            sceneView.planeRenderer.isVisible = false
        } else {
            modelNode.anchor = null
            place_object.text = getString(R.string.place_object)
            sceneView.planeRenderer.isVisible = true
        }

      */
    }

    fun getMillimetersDepth(depthImage: Image, x: Int, y: Int): Int {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.
        val plane = depthImage.planes[0]
        val byteIndex = x * plane.pixelStride + y * plane.rowStride
        val buffer = plane.buffer.order(ByteOrder.nativeOrder())
        val depthSample = buffer.getShort(byteIndex)
        return depthSample.toInt()
    }
}*/