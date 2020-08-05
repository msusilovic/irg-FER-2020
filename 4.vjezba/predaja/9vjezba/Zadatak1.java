package sjencanje;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import linearna.IVector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Zadatak1 {

    //odreduje vrstu sjencanje, false je za kontinuirano, true za gouraudovo
    private static boolean sjencanje = false;
    private static boolean z = false;

    public static void main(String[] args) {

        String name = args[0];
        ObjectModel model = new ObjectModel();
        EyePosition eye = new EyePosition(4, 3,1);

        try (BufferedReader br = new BufferedReader(new FileReader(name))) {

            List<Vertex3D> vertexes = new ArrayList<>();
            List<Face3D> faces = new ArrayList<>();
            model.setFaces(faces);
            model.setVertexes(vertexes);
            String line = br.readLine();
            while(line != null) {
                if(line.startsWith("v")) {
                    String[] parts = line.trim().split("\\s+");
                    Vertex3D v = new Vertex3D(Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]));
                    vertexes.add(v);

                }else if (line.startsWith("f")) {
                    String[] parts = line.trim().split("\\s+");
                    Face3D f = new Face3D(Integer.parseInt(parts[1])-1,
                            Integer.parseInt(parts[2])-1,
                            Integer.parseInt(parts[3])-1);
                    model.addFace3D(f);
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        SwingUtilities.invokeLater(new Runnable () {
            @Override
            public void run() {
                model.normalize();
                model.calculateNormalsOfVertices();

                GLProfile glProfile = GLProfile.getDefault();
                GLCapabilities glCapabilities = new GLCapabilities(glProfile);
                final GLCanvas glCanvas = new GLCanvas(glCapabilities);

                eye.setCanvas(glCanvas);
                glCanvas.addKeyListener(eye.getKeyAdapter());

                glCanvas.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_K){
                            sjencanje = false;
                            glCanvas.display();
                        }else if (e.getKeyCode() == KeyEvent.VK_G) {
                            sjencanje = true;
                            glCanvas.display();
                        }else if (e.getKeyCode() == KeyEvent.VK_Z) {
                            z = !z;
                            if (z) {
                                System.out.println("Z-spremnik ukljucen");
                            }else{
                                System.out.println("Z-spremnik iskljucen");
                            }
                            glCanvas.display();
                        }
                    }
                });

                glCanvas.addGLEventListener(new GLEventListener() {
                    @Override
                    public void init(GLAutoDrawable glAutoDrawable) {
                    }

                    @Override
                    public void dispose(GLAutoDrawable glAutoDrawable) {
                    }

                    @Override
                    public void display(GLAutoDrawable glAutoDrawable) {
                        GL2 gl2 = glAutoDrawable.getGL().getGL2();

                        gl2.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
                        gl2.glEnable(GL2.GL_CULL_FACE);
                        gl2.glCullFace(GL2.GL_BACK);
                        gl2.glClearColor(1, 1, 1, 0);

                        //uporaba z spremnika
                        if(z) {
                            gl2.glEnable(GL2.GL_DEPTH_TEST);
                            gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                        }else{
                            gl2.glClear(GL.GL_COLOR_BUFFER_BIT);
                        }

                        gl2.glLoadIdentity();

                        GLU glu = new GLU();
                        IVector v = eye.getEyeVector();
                        glu.gluLookAt(v.get(0), v.get(1), v.get(2), 0f, 0f, 0f, 0f, 1f, 0f);
                        gl2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

                        gl2.glColor3f(0, 0, 0);
                        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[] {1f, 1f, 1f, 1f} , 0);
                        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[] {1f, 1f, 1f, 1f} , 0);
                        gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.01f, 0.01f, 0.01f, 1f} , 0);
                        gl2.glMaterialf(GL2.GL_FRONT, GL2. GL_SHININESS, 96f);

                        for (Face3D face : model.getFaces()) {
                            Vertex3D[] vertices = face.getVertices();
                            gl2.glBegin(GL2.GL_POLYGON);
                            if(sjencanje){
                                gl2.glNormal3f((float)vertices[0].getA(), (float)vertices[0].getB(), (float)vertices[0].getC());
                                gl2.glVertex3f((float)vertices[0].getX(), (float)vertices[0].getY(), (float)vertices[0].getZ());
                                gl2.glNormal3f((float)vertices[1].getA(), (float)vertices[1].getB(), (float)vertices[1].getC());
                                gl2.glVertex3f((float)vertices[1].getX(), (float)vertices[1].getY(), (float)vertices[1].getZ());
                                gl2.glNormal3f((float)vertices[2].getA(), (float)vertices[2].getB(), (float)vertices[2].getC());
                                gl2.glVertex3f((float)vertices[2].getX(), (float)vertices[2].getY(), (float)vertices[2].getZ());
                            }else{
                                double norm = Math.sqrt(face.getA() * face.getA() + face.getB() * face.getB() + face.getC() * face.getC());
                                gl2.glNormal3f((float)(face.getA()/norm), (float)(face.getB()/norm), (float)(face.getC()/norm));
                                gl2.glVertex3f((float)vertices[0].getX(), (float)vertices[0].getY(), (float)vertices[0].getZ());
                                gl2.glNormal3f((float)(face.getA()/norm), (float)(face.getB()/norm), (float)(face.getC()/norm));
                                gl2.glVertex3f((float)vertices[1].getX(), (float)vertices[1].getY(), (float)vertices[1].getZ());
                                gl2.glNormal3f((float)(face.getA()/norm), (float)(face.getB()/norm), (float)(face.getC()/norm));
                                gl2.glVertex3f((float)vertices[2].getX(), (float)vertices[2].getY(), (float)vertices[2].getZ());
                            }
                            gl2.glEnd();
                        }
                    }


                    @Override
                    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
                        GL2 gl2 = glAutoDrawable.getGL().getGL2();
                        gl2.glMatrixMode(GL2.GL_PROJECTION);
                        gl2.glLoadIdentity();
                        gl2.glFrustum(-0.5f, 0.5,-0.5f, 0.5f, 1, 100);

                        gl2.glMatrixMode(GL2.GL_MODELVIEW);
                        gl2.glViewport(0,0, width, height);

                        gl2.glEnable(GL2.GL_LIGHTING);
                        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {4f, 5f, 3f, 1f}, 0);
                        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.2f, 0.2f, 0.2f, 1f}, 0);
                        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {0.9f, 0.9f, 0f, 1f}, 0);
                        gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[] {0f, 0f, 0f, 1f}, 0);
                        gl2.glEnable(GL2.GL_LIGHT0);
                    }
                });

                final JFrame jFrame = new JFrame(
                        "Sedmi zadatak");
                jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                jFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        jFrame.dispose();
                        System.exit(0);
                    }
                });
                jFrame.getContentPane().add(glCanvas, BorderLayout.CENTER);
                jFrame.setSize(640, 480);
                jFrame.setVisible(true);
                glCanvas.requestFocusInWindow();
            }
        });
    }

}