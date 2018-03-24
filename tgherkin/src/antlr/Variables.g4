lexer grammar Variables;
@header {package nl.ou.testar.tgherkin.gen;}
BOOLEAN_VARIABLE_NAME :
   'Rendered'
 | 'UIAIsContentElement'
 | 'UIAVerticallyScrollable'
 | 'UIAHorizontallyScrollable'
 | 'HasStandardMouse'
 | 'HasStandardKeyboard'
 | 'UIAIsControlElement'
 | 'Blocked'
 | 'UIAIsEnabled'
 | 'IsRunning'
 | 'UIAScrollPattern'
 | 'UIAIsKeyboardFocusable'
 | 'NotResponding'
 | 'UIAIsWindowModal'
 | 'Enabled'
 | 'UIAIsOffscreen'
 | 'Foreground'
 | 'UIAIsTopmostWindow'
 | 'UIAHasKeyboardFocus'
 | 'Modal'
;
NUMBER_VARIABLE_NAME :
   'ActionDelay'
 | 'UIAControlType'
 | 'Shape.height'
 | 'Shape.width'
 | 'ActionDuration'
 | 'TimeStamp'
 | 'UIAProcessId'
 | 'UIAScrollVerticalPercent'
 | 'HANDLE'
 | 'MaxZIndex'
 | 'UIAScrollVerticalViewSize'
 | 'UIAWindowInteractionState'
 | 'UIAScrollHorizontalViewSize'
 | 'ZIndex'
 | 'UIAWindowVisualState'
 | 'UIAOrientation'
 | 'MinZIndex'
 | 'UIACulture'
 | 'Shape.x'
 | 'Shape.y'
 | 'PID'
 | 'Angle'
 | 'UIANativeWindowHandle'
 | 'UIAScrollHorizontalPercent'
;
STRING_VARIABLE_NAME :
   'ValuePattern'
 | 'RunningProcesses'
 | 'ToolTipText'
 | 'TargetID'
 | 'StandardKeyboard'
 | 'Desc'
 | 'StandardMouse'
 | 'UIAFrameworkId'
 | 'UIAAccessKey'
 | 'Abs(R,T)ID'
 | 'HitTester'
 | 'UIAProviderDescription'
 | 'UIAAutomationId'
 | 'ScreenshotPath'
 | 'UIALocalizedControlType'
 | 'Representation'
 | 'OracleVerdict'
 | 'Role'
 | 'UID'
 | 'Visualizer'
 | 'Resources'
 | 'AbstractID'
 | 'SystemState'
 | 'Shape'
 | 'Slider'
 | 'UIAClassName'
 | 'WidgetMap'
 | 'UIAItemStatus'
 | 'Abs(R,T,P)ID'
 | 'StdIn'
 | 'StdErr'
 | 'UIAHelpText'
 | 'DynDesc'
 | 'UIAAcceleratorKey'
 | 'Path'
 | 'UIARuntimeId'
 | 'UIABoundingRectangle'
 | 'ExecutedAction'
 | 'UIAName'
 | 'Title'
 | 'Text'
 | 'Targets'
 | 'Abs(R)ID'
 | 'ActionSet'
 | 'UIAItemType'
 | 'SystemActivator'
 | 'StdOut'
 | 'ConcreteID'
 | 'ProcessHandles'
;
