package jp.techacademy.ayaka.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    textView.text = "画像の取得が許可されていません"
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("CURSOR","first:${cursor.getPosition()}")
                imageView.setImageURI(imageUri)
        }

        //進むボタンで1つ先の画像を表示
        next_button.setOnClickListener {
                if (cursor.moveToNext()) {
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageView.setImageURI(imageUri)
                } else if (cursor.moveToFirst()) {
                    //最後の画像の表示時に、進むボタンをタップすると、最初の画像が表示される
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageView.setImageURI(imageUri)
                }
            }


        //戻るボタンで1つ前の画像を表示
        back_button.setOnClickListener {
            if(cursor.moveToPrevious()) {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)
            } else if(cursor.moveToLast()){
                //最初の画像の表示時に、戻るボタンをタップすると、最後の画像が表示される
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)
            }
        }

        //再生ボタンをタップすると2秒後に自動送りが始まり、2秒毎にスライドさせる
        start_pause_button.setOnClickListener {
            if (mTimer == null) {
                // タイマーの作成
                mTimer = Timer()

                //再生ボタンをタップすると、ボタンの表示が「停止」にる
                start_pause_button.text = "停止"
                //自動送りの間は、進むボタンと戻るボタンはタップ不可
                next_button.visibility = View.INVISIBLE
                back_button.visibility = View.INVISIBLE

                // タイマーの始動
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 0.1
                        mHandler.post {
                            if (cursor.moveToNext()) {
                                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                val id = cursor.getLong(fieldIndex)
                                val imageUri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )

                                imageView.setImageURI(imageUri)
                            } else if (cursor.moveToFirst()) {
                                //最後の画像の表示後は、最初の画像が表示される
                                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                val id = cursor.getLong(fieldIndex)
                                val imageUri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )

                                imageView.setImageURI(imageUri)
                            }
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
            } else if(mTimer != null){
                //停止ボタンをタップすると、ボタンの表示が「再生」にる
                start_pause_button.text = "再生"
                //停止ボタンをタップすると自動送りが止まり、進むボタンと戻るボタンをタップ可能にする
                next_button.visibility = View.VISIBLE
                back_button.visibility = View.VISIBLE
                mTimer!!.cancel()
                mTimer = null
            }
        }

        //cursor.close()　アクティビティが破棄されるタイミング　onDestroy, onPause→onResumeで復帰
    }
}
