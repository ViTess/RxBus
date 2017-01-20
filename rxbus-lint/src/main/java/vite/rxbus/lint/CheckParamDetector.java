package vite.rxbus.lint;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collection;
import java.util.List;

import lombok.ast.Node;

/**
 * Created by trs on 17-1-16.
 */
public class CheckParamDetector extends Detector implements Detector.JavaScanner{
    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        return super.getApplicableNodeTypes();
    }
}
