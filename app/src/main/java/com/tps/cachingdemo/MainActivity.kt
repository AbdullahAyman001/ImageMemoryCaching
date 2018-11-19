package com.tps.cachingdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import com.androidnetworking.AndroidNetworking
import com.squareup.picasso.Picasso
import com.tps.cacheutilrequests.BitmapImageCaching

class MainActivity : AppCompatActivity() {

    lateinit var img: ImageView
    lateinit var img2: ImageView
    lateinit var list: ArrayList<String>
    var count = 0
    var currentIndex = 0
    lateinit var imageLoadingRef: BitmapImageCaching
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(getApplicationContext())
        imageLoadingRef = BitmapImageCaching.getInstance()
        list = ArrayList<String>()
        list.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/00152/opgs/edr/ncam/NLA_410988823EDR_F0051954NCAM00354M_.JPG")
        list.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/00152/opgs/edr/ncam/NLA_410988037EDR_D0051916TRAV00040M_.JPG")
        list.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/00152/opgs/edr/ncam/NLA_410988681EDR_F0051954NCAM05144M_.JPG")
        var download = findViewById(R.id.button) as Button
        img = findViewById(R.id.image) as ImageView
        img2 = findViewById(R.id.image2) as ImageView
        download.setOnClickListener {

            Picasso.get()
                .load(list.get(count))
                .into(img2)
            load()
            count++
            if (count >= list.size)
                count = 0
        }
        findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            imageLoadingRef.cancelRequest(list.get(currentIndex), img)
        }
    }


    private fun load() {
        currentIndex = count
        imageLoadingRef
            .allowCaching(true)
            .loadUrl(list.get(count))
            .setImageRef(img)
            .execute()
    }
}
