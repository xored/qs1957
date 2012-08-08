package com.xored.qs1957.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class QS1957View extends ViewPart {
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		ScrolledForm form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());

		form.setText("Q7 Testing View");

		GridLayoutFactory.fillDefaults().applyTo(form.getBody());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form.getBody());

		final Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED
						| Section.TWISTIE | Section.NO_TITLE_FOCUS_BOX);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

		section.setText("QS-1957 Test");
		section.setDescription("First column has cell editor, second have mouse listener");
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(section);

		viewer = new TableViewer(section);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(viewer.getControl());
		section.setClient(viewer.getControl());
		viewer.setContentProvider(new SampleTableContentProvider());

		createFirstColumn();
		createSecondColumn();

		viewer.getTable().addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				handleMouseEvent(new Point(event.x, event.y));

			}
		});

		viewer.setInput(SampleData.createSample());

		final Control control = viewer.getControl();
		section.addExpansionListener(new IExpansionListener() {
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				control.setVisible(section.isExpanded());
				GridDataFactory.fillDefaults().grab(true, section.isExpanded())
						.applyTo(section);
				section.getParent().layout();
			}
		});

	}

	private void createSecondColumn() {
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setText("Value");
		col2.getColumn().setWidth(400);
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SampleData) element).value;
			}
		});
	}

	private void createFirstColumn() {
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SampleData) element).name;
			}
		});
		col1.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				((SampleData) element).name = (String) value;
				viewer.update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				return ((SampleData) element).name;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		col1.getColumn().setText("Name");
		col1.getColumn().setWidth(200);
	}

	private void handleMouseEvent(Point point) {
		Table table = viewer.getTable();
		TableItem item = table.getItem(point);
		for (int i = 0; i < table.getColumnCount(); i++) {
			Rectangle bounds = item.getBounds(i);
			if (bounds.contains(point) && i == 1) {
				showPopup(item, bounds);
			}
		}
	}

	private Shell proposals;

	private void showPopup(final TableItem item, Rectangle cellBounds) {
		if (proposals != null && !proposals.isDisposed()) {
			proposals.close();
			proposals.dispose();
			proposals = null;
		}
		proposals = new Shell(item.getParent().getDisplay().getActiveShell(),
				SWT.ON_TOP | SWT.RESIZE);
		Point leftDown = item.getParent().toDisplay(
				new Point(cellBounds.x, cellBounds.y + cellBounds.height));
		Rectangle shellBounds = new Rectangle(leftDown.x, leftDown.y + 10,
				cellBounds.width, 200);
		proposals.setBounds(shellBounds);
		final Table table = new Table(proposals, SWT.H_SCROLL | SWT.V_SCROLL);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(proposals);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(table);

		for (String str : new String[] { "foo", "bar", "baz" }) {
			TableItem ti = new TableItem(table, 0);
			ti.setText(str);
		}

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((SampleData) item.getData()).value = table.getSelection()[0]
						.getText();
				viewer.refresh();
				proposals.close();
				proposals.dispose();
				proposals = null;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		proposals.open();
	}

	public void setFocus() {
	}

	private static class SampleData {
		public String name;
		public String value;

		public SampleData(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public static List<SampleData> createSample() {
			return new ArrayList<SampleData>(Arrays.asList(new SampleData(
					"Name1", "value1"), new SampleData("Name2", "value2")));
		}
	}

	private static class SampleTableContentProvider implements
			IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<SampleData>) inputElement).toArray();
		}

	}

	private TableViewer viewer;
}