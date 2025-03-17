package com.angcyo.opengl

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.angcyo.library.L
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/17
 */
open class OpenGLRenderer : GLSurfaceView.Renderer {

    /**渲染的背景颜色*/
    var backgroundColor: Int = Color.YELLOW

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //
        // Create a minimum supported OpenGL ES context, then check:
        gl?.glGetString(GL10.GL_VERSION).also {
            //OpenGL ES 3.2 v1.r50p0-00eac0.23a3e850fb3835a51f4357e034c00a3d
            L.w("Version: $it")
        }
        gl?.glGetString(GL10.GL_EXTENSIONS).also {
            //GL_EXT_debug_marker GL_ARM_rgba8 GL_ARM_mali_shader_binary GL_ARM_debug_assert GL_OES_depth24 GL_OES_depth_texture GL_OES_depth_texture_cube_map GL_OES_packed_depth_stencil GL_OES_rgb8_rgba8 GL_EXT_read_format_bgra GL_OES_compressed_paletted_texture GL_OES_compressed_ETC1_RGB8_texture GL_OES_standard_derivatives GL_OES_EGL_image GL_OES_EGL_image_external GL_OES_EGL_image_external_essl3 GL_OES_EGL_sync GL_OES_texture_npot GL_OES_vertex_half_float GL_OES_required_internalformat GL_OES_vertex_array_object GL_OES_mapbuffer GL_EXT_texture_format_BGRA8888 GL_EXT_texture_rg GL_EXT_texture_type_2_10_10_10_REV GL_OES_fbo_render_mipmap GL_OES_element_index_uint GL_EXT_shadow_samplers GL_OES_texture_compression_astc GL_KHR_texture_compression_astc_ldr GL_KHR_texture_compression_astc_hdr GL_KHR_texture_compression_astc_sliced_3d GL_EXT_texture_compression_astc_decode_mode GL_EXT_texture_compression_astc_decode_mode_rgb9e5 GL_KHR_debug GL_EXT_occlusion_query_boolean GL_EXT_disjoint_timer_query GL_EXT_blend_minmax GL_EXT_discard_framebuffer GL_OES_get_program_binary GL_OES_texture_3D GL_EXT_texture_storage GL_EXT_multisampled_render_to_texture GL_EXT_multisampled_render_to_texture2 GL_OES_surfaceless_context GL_OES_texture_stencil8 GL_EXT_shader_pixel_local_storage GL_ARM_shader_framebuffer_fetch GL_ARM_shader_framebuffer_fetch_depth_stencil GL_ARM_mali_program_binary GL_EXT_sRGB GL_EXT_sRGB_write_control GL_EXT_texture_sRGB_decode GL_EXT_texture_sRGB_R8 GL_EXT_texture_sRGB_RG8 GL_KHR_blend_equation_advanced GL_KHR_blend_equation_advanced_coherent GL_OES_texture_storage_multisample_2d_array GL_OES_shader_image_atomic GL_EXT_robustness GL_EXT_draw_buffers_indexed GL_OES_draw_buffers_indexed GL_EXT_texture_border_clamp GL_OES_texture_border_clamp GL_EXT_texture_cube_map_array GL_OES_texture_cube_map_array GL_OES_sample_variables GL_OES_sample_shading GL_OES_shader_multisample_interpolation GL_EXT_shader_io_blocks GL_OES_shader_io_blocks GL_EXT_tessellation_shader GL_OES_tessellation_shader GL_EXT_primitive_bounding_box GL_OES_primitive_bounding_box GL_EXT_geometry_shader GL_OES_geometry_shader GL_ANDROID_extension_pack_es31a GL_EXT_gpu_shader5 GL_OES_gpu_shader5 GL_EXT_texture_buffer GL_OES_texture_buffer GL_EXT_copy_image GL_OES_copy_image GL_EXT_shader_non_constant_global_initializers GL_EXT_color_buffer_half_float GL_EXT_unpack_subimage GL_EXT_color_buffer_float GL_EXT_float_blend GL_EXT_YUV_target GL_OVR_multiview GL_OVR_multiview2 GL_OVR_multiview_multisampled_render_to_texture GL_KHR_robustness GL_KHR_robust_buffer_access_behavior GL_EXT_draw_elements_base_vertex GL_OES_draw_elements_base_vertex GL_EXT_protected_textures GL_EXT_buffer_storage GL_EXT_external_buffer GL_EXT_EGL_image_array GL_EXT_clear_texture GL_ARM_shader_core_properties GL_EXT_texture_filter_anisotropic GL_OES_texture_float_linear GL_ARM_texture_unnormalized_coordinates GL_EXT_shader_framebuffer_fetch GL_EXT_clip_control GL_EXT_polygon_offset_clamp GL_EXT_EGL_image_storage
            L.w("extensions: $it")
        }
        // The version format is displayed as: "OpenGL ES <major>.<minor>"
        // followed by optional content provided by the implementation.
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        L.w("onSurfaceChanged: $width x $height") // 1080 x 736
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 9f)
        //Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 3f)
        //Matrix.frustumM(projectionMatrix, 0, -10f, 10f, -10f, 10f, 1f, 3f)

        //Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)

        //GLES20.glViewport(0, 0, width, height)
        //val ratio: Float = width.toFloat() / height.toFloat()
        // create a projection matrix from device screen geometry
        //Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Set the background frame color
        GLES20.glClearColor(
            backgroundColor.red / 255f,
            backgroundColor.green / 255f,
            backgroundColor.blue / 255f,
            backgroundColor.alpha / 255f
        )
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}