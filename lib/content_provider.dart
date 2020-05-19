import 'dart:async';

import 'package:flutter/services.dart';

import 'dart:io';

class ContentProviderPlugin {
  static const MethodChannel _channel = const MethodChannel('flutter_plugin');

  static Future<List<Map<dynamic, dynamic>>> getContentValue(String uri) async {
    var parameters = {'uri': '$uri'};
    if (!_isValidUri(uri)) {
      throw Exception("invalid data");
    }
    if (Platform.isIOS) {
      throw Exception("Not impleted for ios");
    }
    List<dynamic> data =
        await _channel.invokeMethod('getContent', parameters);
    return data.cast<Map<dynamic, dynamic>>();
  }

  static Future<void> insertContentValue(String uri, dynamic data) async {
    var parameters = Map();

    parameters['uri'] = '$uri';
    if (!_isValidUri(uri) || data == null) {
      throw Exception("invalid data");
    }
    if (Platform.isIOS) {
      throw Exception("Not impleted for ios");
    }
    try {
      parameters['contentValues'] = data;
      await _channel.invokeMethod('insertContent', parameters);
    } catch (exception) {}
  }

  static Future<void> updateContentValue(String uri, dynamic data,
      {String where, List<String> whereArgs}) async {
    if (!_isValidUri(uri) || data == null) {
      throw Exception("invalid data");
    }
    if (where != null && where.isEmpty) {
      throw Exception("invalid data");
    }
    if (whereArgs != null && whereArgs.isEmpty) {
      throw Exception("invalid data");
    }
    if (Platform.isIOS) {
      throw Exception("Not impleted for ios");
    }
    var parameters = Map();

    argparametersuments['uri'] = '$uri';
    if (where != null) {
      parameters['where'] = '$where';
    }
    parameters["contentValues"] = data;
    if (whereArgs != null) {
      parameters["whereArgs"] = whereArgs;
    }
    try {
      await _channel.invokeMethod('updateContent', parameters);
    } catch (exception) {}
  }

  static Future<void> deleteContentValue(String uri, dynamic data, String where,
      List<String> selectionArgs) async {
    if (!_isValidUri(uri) || data == null) {
      throw Exception("invalid data");
    }
    if (where != null && where.isEmpty) {
      throw Exception("invalid data");
    }
    if (selectionArgs != null && selectionArgs.isEmpty) {
      throw Exception("invalid data");
    }
    if (Platform.isIOS) {
      throw Exception("Not impleted for ios");
    }

    var parameters = Map();
    try {
      parameters['uri'] ='$uri';

      if(where != null){
        parameters['where'] = '$where';
      }

      if(selectionArgs != null){
        parameters['whereArgs'] = selectionArgs;
      }
      await _channel.invokeMethod('deleteContent',
          parameters);
    } catch (exception) {}
  }

  static bool _isValidUri(String uri) => (uri != null && uri.isNotEmpty);
}
