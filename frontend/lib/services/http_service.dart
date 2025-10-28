import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/api_response.dart';
import '../utils/api_constants.dart';

class HttpService {
  static final HttpService _instance = HttpService._internal();
  factory HttpService() => _instance;
  HttpService._internal();

  static const Duration _timeout = Duration(seconds: 30);

  // Get headers with authentication if available
  Future<Map<String, String>> _getHeaders({bool includeAuth = true}) async {
    Map<String, String> headers = Map.from(ApiConstants.headers);
    
    if (includeAuth) {
      final prefs = await SharedPreferences.getInstance();
      final accessToken = prefs.getString(ApiConstants.accessTokenKey);
      if (accessToken != null) {
        headers['Authorization'] = 'Bearer $accessToken';
      }
    }
    
    return headers;
  }

  // Handle HTTP response
  ApiResponse<T> _handleResponse<T>(http.Response response, T Function(dynamic)? fromJson) {
    try {
      if (response.statusCode >= 200 && response.statusCode < 300) {
        // Try to parse as JSON first
        try {
          final dynamic jsonResponse = json.decode(response.body);
          
          // If response is a Map and has typical data structure
          if (jsonResponse is Map<String, dynamic>) {
            if (fromJson != null) {
              return ApiResponse.success(fromJson(jsonResponse), 
                  message: jsonResponse['message']);
            } else {
              return ApiResponse.success(jsonResponse as T, 
                  message: jsonResponse['message']);
            }
          } else {
            // Response is not a map (e.g., plain string)
            return ApiResponse.success(jsonResponse as T);
          }
        } catch (e) {
          // If JSON parsing fails, treat as plain text
          return ApiResponse.success(response.body as T);
        }
      } else {
        // Error response
        try {
          final Map<String, dynamic> jsonResponse = json.decode(response.body);
          return ApiResponse.error(
            jsonResponse['message'] ?? 'HTTP Error ${response.statusCode}'
          );
        } catch (e) {
          return ApiResponse.error('HTTP Error ${response.statusCode}: ${response.body}');
        }
      }
    } catch (e) {
      return ApiResponse.error('Failed to parse response: $e');
    }
  }

  // GET request
  Future<ApiResponse<T>> get<T>(
    String endpoint, {
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
      
      final response = await http.get(url, headers: headers).timeout(_timeout);
      return _handleResponse<T>(response, fromJson);
    } on SocketException {
      return ApiResponse.error('No internet connection');
    } on HttpException {
      return ApiResponse.error('HTTP error occurred');
    } on FormatException {
      return ApiResponse.error('Bad response format');
    } catch (e) {
      return ApiResponse.error('Unexpected error: $e');
    }
  }

  // POST request
  Future<ApiResponse<T>> post<T>(
    String endpoint, {
    dynamic body,
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
      
      final response = await http.post(
        url,
        headers: headers,
        body: body != null ? json.encode(body) : null,
      ).timeout(_timeout);
      
      return _handleResponse<T>(response, fromJson);
    } on SocketException {
      return ApiResponse.error('No internet connection');
    } on HttpException {
      return ApiResponse.error('HTTP error occurred');
    } on FormatException {
      return ApiResponse.error('Bad response format');
    } catch (e) {
      return ApiResponse.error('Unexpected error: $e');
    }
  }

  // PUT request
  Future<ApiResponse<T>> put<T>(
    String endpoint, {
    dynamic body,
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
      
      final response = await http.put(
        url,
        headers: headers,
        body: body != null ? json.encode(body) : null,
      ).timeout(_timeout);
      
      return _handleResponse<T>(response, fromJson);
    } on SocketException {
      return ApiResponse.error('No internet connection');
    } on HttpException {
      return ApiResponse.error('HTTP error occurred');
    } on FormatException {
      return ApiResponse.error('Bad response format');
    } catch (e) {
      return ApiResponse.error('Unexpected error: $e');
    }
  }

  // DELETE request
  Future<ApiResponse<T>> delete<T>(
    String endpoint, {
    bool includeAuth = true,
    T Function(dynamic)? fromJson,
  }) async {
    try {
      final headers = await _getHeaders(includeAuth: includeAuth);
      final url = Uri.parse('${ApiConstants.baseUrl}$endpoint');
      
      final response = await http.delete(url, headers: headers).timeout(_timeout);
      return _handleResponse<T>(response, fromJson);
    } on SocketException {
      return ApiResponse.error('No internet connection');
    } on HttpException {
      return ApiResponse.error('HTTP error occurred');
    } on FormatException {
      return ApiResponse.error('Bad response format');
    } catch (e) {
      return ApiResponse.error('Unexpected error: $e');
    }
  }

  // Clear stored tokens
  Future<void> clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(ApiConstants.accessTokenKey);
    await prefs.remove(ApiConstants.refreshTokenKey);
    await prefs.remove(ApiConstants.userIdKey);
  }

  // Store tokens
  Future<void> storeTokens(String accessToken, String refreshToken, String userId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(ApiConstants.accessTokenKey, accessToken);
    await prefs.setString(ApiConstants.refreshTokenKey, refreshToken);
    await prefs.setString(ApiConstants.userIdKey, userId);
  }
}