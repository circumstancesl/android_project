package com.example.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText: EditText = findViewById(R.id.et_search)
        val buttonSearch: Button = findViewById(R.id.button_search)
        val buttonFavorites = findViewById<Button>(R.id.button_favorites)

        buttonFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.rview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MovieAdapter(mutableListOf())
        recyclerView.adapter = adapter

        buttonSearch.setOnClickListener {
            val keyword = editText.text.toString()
            if (keyword.isNotEmpty()) {
                fetchMovies(keyword)
            }
        }
    }

    private fun fetchMovies(keyword: String) {
        lifecycleScope.launch {
            val response = fetchMovieData(keyword)
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val movieResponse = Gson().fromJson(responseBody, MovieResponse::class.java)
                adapter = MovieAdapter(movieResponse.films)
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : MovieAdapter.OnItemClickListener {
                    override fun onItemClick(movie: Movie) {
                        val sharedPreferences = getSharedPreferences("favorite_movies", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val movieData = "${movie.nameRu} (${movie.year})"
                        editor.putString(movie.nameRu, movieData)
                        editor.apply()

                        Toast.makeText(this@MainActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun fetchMovieData(keyword: String): Response {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://kinopoiskapiunofficial.tech/api/v2.1/films/search-by-keyword?keyword=$keyword")
                .header("X-API-KEY", "7f3bad7b-3c9a-4910-99ec-5a67c2348dce") // api-key
                .build()
            client.newCall(request).execute()
        }
    }
}

data class MovieResponse(
    val films: List<Movie>
)
